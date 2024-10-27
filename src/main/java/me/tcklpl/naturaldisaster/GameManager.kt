package me.tcklpl.naturaldisaster

import me.tcklpl.naturaldisaster.disasters.Disaster
import me.tcklpl.naturaldisaster.disasters.DisasterManager
import me.tcklpl.naturaldisaster.exceptions.InvalidGameStartException
import me.tcklpl.naturaldisaster.map.ArenaManager
import me.tcklpl.naturaldisaster.map.DisasterMap
import me.tcklpl.naturaldisaster.player.cPlayer.CPlayer
import me.tcklpl.naturaldisaster.player.ingamePlayer.ArenaPlayerManager
import me.tcklpl.naturaldisaster.util.ActionBar
import me.tcklpl.naturaldisaster.util.BiomeUtils.randomizeBiome
import me.tcklpl.naturaldisaster.util.NamesAndColors.pickRandomColors
import me.tcklpl.naturaldisaster.util.NamesAndColors.pickRandomNames
import me.tcklpl.naturaldisaster.util.PlayerUtils.healPlayer
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import java.util.LinkedList
import java.util.Queue
import java.util.Random
import java.util.concurrent.atomic.AtomicInteger

class GameManager(private val main: JavaPlugin) {
    var currentStatus: GameStatus? = null

    val disasterManager = DisasterManager()
    val arenaManager = ArenaManager()
    private var arenaPlayerManager: ArenaPlayerManager? = null

    var currentDisaster: Disaster? = null
    var currentMap: DisasterMap? = null

    private val lastPlayQueueSize = 3
    private val lastDisasters: Queue<Disaster> = LinkedList<Disaster>()
    private val lastMaps: Queue<DisasterMap> = LinkedList<DisasterMap>()

    private val r = Random()

    private val timeoutMinutes = 3L;
    private var startupTaskId = -1
    private var timeoutTaskId = -1

    fun isIngame(): Boolean {
        return currentStatus == GameStatus.IN_GAME
    }

    fun pickNextGame() {
        if (currentStatus != GameStatus.IN_LOBBY) throw InvalidGameStartException()

        selectNextMap()
        selectNextDisaster()

        currentDisaster!!.map = currentMap!!

        ActionBar(ChatColor.GOLD.toString() + "Próximo mapa: " + currentMap!!.name).sendToAll()
        arenaManager.loadArenaWorld(currentMap!!) { startNextGame() }
    }

    /**
     * Selects the next map if it's not already defined.
     */
    private fun selectNextMap() {
        if (currentMap != null) return

        val arenas = arenaManager.arenas
        var i = r.nextInt(arenas.size)
        if (lastMaps.size > lastPlayQueueSize) lastMaps.remove()
        do currentMap = arenas[(i++) % arenas.size]
        while (lastMaps.contains(currentMap))

        lastMaps.add(currentMap)
    }

    /**
     * Selects the next disaster if it's not already defined
     */
    private fun selectNextDisaster() {
        if (currentDisaster != null) return

        val disasters = disasterManager.getPlayableDisasters()
        var i = r.nextInt(disasters.size)
        if (lastDisasters.size > lastPlayQueueSize) lastDisasters.remove()
        do currentDisaster = disasters[(i++) % disasters.size]
        while (lastDisasters.contains(currentDisaster))

        lastDisasters.add(currentDisaster)
    }

    /**
     * Starts the next game
     * THIS FUNCTION SHOULD ONLY BE CALLED AFTER _ALL_ CHUNKS ARE LOADED
     * Broadcasts a countdown to all players and starts the disaster afterward
     */
    private fun startNextGame() {
        arenaPlayerManager = ArenaPlayerManager()
        currentStatus = GameStatus.STARTING
        currentMap?.addAllPlayersToArena()

        disguisePlayers()

        // Broadcast 5s countdown
        val counter = AtomicInteger(5)
        startupTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(main, Runnable {
            ActionBar(ChatColor.GOLD.toString() + "Começando em: " + counter.get()).sendToAll()
            if (counter.getAndDecrement() <= 0) Bukkit.getScheduler().cancelTask(startupTaskId)
        }, 20L, 20L)

        currentMap?.setArenaBiome(randomizeBiome(currentDisaster!!.precipitationRequirements))
        currentMap?.teleportPlayersToSpawns()
        for (p in currentMap!!.playersInArena) p.isInvulnerable = true

        // Do some initial calculatrion for the current disaster (construct block indies, set precipitation etc.)
        currentDisaster?.setupDisaster()

        // Start the disaster after the countdown ends
        Bukkit.getScheduler().scheduleSyncDelayedTask(main, Runnable {
            currentDisaster?.startDisaster()
            ActionBar("${ChatColor.RED}Boa sorte!").sendToAll()
            currentStatus = GameStatus.IN_GAME
            for (all in currentMap!!.playersInArena) {
                all.playSound(all.getLocation(), Sound.EVENT_RAID_HORN, 1f, 1f)
                all.isInvulnerable = false
                healPlayer(all)
            }

            // Register the task for the timeout
            Bukkit.getScheduler().scheduleSyncDelayedTask(main, { this.endByTimeout() }, timeoutMinutes * 20 * 60)
        }, 130L)
    }

    /**
     * Disguises all players in the arena with a random name and a random skin
     */
    fun disguisePlayers() {
        if (currentMap == null) return

        val playerCount = currentMap!!.playersInArena.size
        val names = pickRandomNames(playerCount)
        val colors = pickRandomColors(playerCount)
        for (i in 0 until playerCount) {
            val p = currentMap!!.playersInArena[i]
            arenaPlayerManager!!.disguisePlayer(p, colors[i].toString() + names[i])
        }
    }

    /**
     * Updates the arena when a player dies, possibly triggering the game end
     *
     * @param p The dead player
     */
    fun registerPlayerDeath(p: Player) {
        if (!currentMap!!.playersInArena.contains(p)) return

        // Play sound and update the dead player
        p.playSound(p, Sound.BLOCK_BEACON_DEACTIVATE, 10f, 1f)
        arenaPlayerManager?.returnPlayerToNormal(p)
        currentMap?.playersInArena?.remove(p)

        // If there are at least 2 players still alive the game still goes on
        if (currentMap!!.playersInArena.size > 1) {
            Bukkit.broadcastMessage("${p.displayName}${ChatColor.GRAY}( ${p.name} ) morreu, ainda restam ${currentMap!!.playersInArena.size} jogadores vivos!")
            for (player in currentMap!!.playersInArena) {
                player.sendMessage("${ChatColor.GRAY}+$1 por sobreviver.")

                val cp: CPlayer = NaturalDisaster.instance.cPlayerManager.getCPlayer(player.uniqueId)!!
                cp.money = cp.money + 1
            }
            teleportSpectatorToArena(p)
            return
        }

        // 0-1 Player left alive, the game has ended
        Bukkit.broadcastMessage("${ChatColor.GOLD}O jogo acabou.")
        // Only add a win to the last player actually exists
        // This will be false for example when solo testing a disaster, you'll die and there'd be no more players
        if (currentMap!!.playersInArena.size == 1) {
            val winner = currentMap!!.playersInArena[0]
            Bukkit.getScheduler().runTaskLater(
                main,
                Runnable { Bukkit.broadcastMessage("${ChatColor.GREEN}${winner.name} venceu!") },
                20L
            )

            val cp: CPlayer = NaturalDisaster.instance.cPlayerManager.getCPlayer(winner.uniqueId)!!
            cp.wins = cp.wins + 1
            cp.money = cp.money + 25
            winner.sendMessage("${ChatColor.GOLD}+$25 por ganhar a partida.")
        }
        endGame()
    }

    /**
     * Ends the current game, undisguising everyone, sending everyone back to the lobby and unloading the current map
     * without saving it
     */
    private fun endGame() {
        currentDisaster!!.stopDisaster()

        // Cancel timeout task
        cancelTimeout()

        // Undisguise everyone
        for (p in currentMap!!.playersInArena) arenaPlayerManager!!.returnPlayerToNormal(p)
        for (p in Bukkit.getOnlinePlayers()) {
            p.setDisplayName(p.name)
            p.setPlayerListName(p.name)
            p.scoreboard = Bukkit.getScoreboardManager()!!.newScoreboard
        }

        // Teleport everyone back to the lobby after 1s
        Bukkit.getScheduler().scheduleSyncDelayedTask(main, Runnable {
            for (p in Bukkit.getOnlinePlayers()) {
                p.gameMode = GameMode.ADVENTURE
                p.teleport(Location(Bukkit.getWorld("worlds/void"), 8.0, 8.0, 8.0))
                p.inventory.clear()
                healPlayer(p)
            }
        }, 20L)

        // Unload the current map after 2s
        Bukkit.getScheduler().scheduleSyncDelayedTask(main, Runnable {
            if (Bukkit.unloadWorld(currentMap!!.pos1.world!!, false)) {
                NaturalDisaster.instance.logger.info("Mundo ${currentMap!!.name} descarregado.")
                for (p in Bukkit.getOnlinePlayers()) {
                    if (p.isOp) p.sendMessage("${ChatColor.GRAY}>> ${ChatColor.DARK_GRAY}[DEBUG INFO] ${ChatColor.GRAY}>> Mundo descarregado.")
                }
            } else NaturalDisaster.instance.logger.severe("Falha ao descarregar mundo $currentMap")
            currentMap = null
            currentDisaster = null
            currentStatus = GameStatus.IN_LOBBY
        }, 40L)
    }

    /**
     * Cancels the timeout task and clears its id
     */
    private fun cancelTimeout() {
        if (timeoutTaskId == -1) return
        Bukkit.getScheduler().cancelTask(timeoutTaskId)
        timeoutTaskId = -1
    }

    /**
     * Teleports a player as a spectator the first alive player in the arena
     *
     * @param p The player to be teleported as a spectator
     */
    fun teleportSpectatorToArena(p: Player) {
        if (currentStatus != GameStatus.IN_GAME) return
        p.gameMode = GameMode.SPECTATOR
        if (!currentMap!!.playersInArena.isEmpty()) p.teleport(currentMap!!.playersInArena[0])
    }

    /**
     * Ends the game by timeout, granting a win to everyone still alive
     */
    fun endByTimeout() {
        Bukkit.broadcastMessage("${ChatColor.YELLOW}Acabou o tempo do mapa, todos ainda vivos ganharam")
        for (p in currentMap!!.playersInArena) {
            val cp: CPlayer = NaturalDisaster.instance.cPlayerManager.getCPlayer(p.uniqueId)!!
            cp.wins = cp.wins + 1
            cp.money = cp.money + 25
            p.sendMessage("${ChatColor.GOLD}+$25 por ganhar a partida.")
        }
        endGame()
    }
}
