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

    private val lastDisasters: Queue<Disaster?> = LinkedList<Disaster?>()
    private val lastMaps: Queue<DisasterMap?> = LinkedList<DisasterMap?>()

    private val r = Random()

    private var startupCounterId = 0

    fun isIngame(): Boolean {
        return currentStatus == GameStatus.IN_GAME
    }

    @Throws(InvalidGameStartException::class)
    fun pickNextGame() {
        if (currentStatus != GameStatus.IN_LOBBY) throw InvalidGameStartException()

        if (currentMap == null) {
            val arenas = arenaManager.arenas
            var i = r.nextInt(arenas.size)
            if (lastMaps.size > 3) lastMaps.remove()
            do currentMap = arenas[(i++) % arenas.size]
            while (lastMaps.contains(currentMap))
        }

        if (currentDisaster == null) {
            val disasters = disasterManager.getPlayableDisasters()
            var i = r.nextInt(disasters.size)
            if (lastDisasters.size > 3) lastDisasters.remove()
            do currentDisaster = disasters[(i++) % disasters.size]
            while (lastDisasters.contains(currentDisaster))
        }

        currentDisaster!!.map = currentMap!!

        ActionBar(ChatColor.GOLD.toString() + "Próximo mapa: " + currentMap!!.name).sendToAll()

        arenaManager.loadArenaWorld(currentMap!!)
    }

    fun startNextGame() {
        arenaPlayerManager = ArenaPlayerManager()
        currentStatus = GameStatus.STARTING
        currentMap!!.addAllPlayersToArena()

        assignRandomNames()

        val counter = AtomicInteger(5)
        startupCounterId = Bukkit.getScheduler().scheduleSyncRepeatingTask(main, Runnable {
            ActionBar(ChatColor.GOLD.toString() + "Começando em: " + counter.get()).sendToAll()
            if (counter.getAndDecrement() <= 0) cancelStartupCounter()
        }, 20L, 20L)

        currentMap!!.setArenaBiome(randomizeBiome(currentDisaster!!.precipitationRequirements))
        currentMap!!.teleportPlayersToSpawns()

        for (p in currentMap!!.playersInArena) p.isInvulnerable = true

        currentDisaster!!.setupDisaster()

        Bukkit.getScheduler().scheduleSyncDelayedTask(main, Runnable {
            currentDisaster!!.startDisaster()
            ActionBar(ChatColor.RED.toString() + "Boa sorte!").sendToAll()
            currentStatus = GameStatus.IN_GAME
            for (all in currentMap!!.playersInArena) {
                all.playSound(all.getLocation(), Sound.EVENT_RAID_HORN, 1f, 1f)
                all.isInvulnerable = false
                healPlayer(all)
            }
        }, 130L)
    }

    fun assignRandomNames() {
        if (currentMap != null) {
            val playerCount = currentMap!!.playersInArena.size
            val names = pickRandomNames(playerCount)
            val colors = pickRandomColors(playerCount)
            for (i in 0 until playerCount) {
                val p = checkNotNull(currentMap!!.playersInArena[i])
                arenaPlayerManager!!.disguisePlayer(p, colors[i].toString() + names[i])
            }
        }
    }

    private fun cancelStartupCounter() {
        Bukkit.getScheduler().cancelTask(startupCounterId)
    }

    fun registerPlayerDeath(p: Player) {
        if (!currentMap!!.playersInArena.contains(p)) return

        p.playSound(p, Sound.BLOCK_BEACON_DEACTIVATE, 10f, 1f)
        arenaPlayerManager!!.returnPlayerToNormal(p)
        currentMap!!.playersInArena.remove(p)

        if (currentMap!!.playersInArena.size <= 1) {
            Bukkit.broadcastMessage(ChatColor.GOLD.toString() + "O jogo acabou.")
            if (currentMap!!.playersInArena.isEmpty()) {
                endGame()
            } else if (currentMap!!.playersInArena.size == 1) {
                val winner = currentMap!!.playersInArena.get(0)
                Bukkit.getScheduler().runTaskLater(
                    main,
                    Runnable { Bukkit.broadcastMessage("${ChatColor.GREEN}${winner.name} venceu!") },
                    20L
                )

                val cp: CPlayer = NaturalDisaster.instance.cPlayerManager.getCPlayer(winner.uniqueId)!!
                cp.wins = cp.wins + 1
                cp.money = cp.money + 25
                winner.sendMessage("${ChatColor.GOLD}+$25 por ganhar a partida.")
                endGame()
            }
        } else {
            Bukkit.broadcastMessage("${p.displayName}${ChatColor.GRAY}( ${p.name} ) morreu, ainda restam ${currentMap!!.playersInArena.size} jogadores vivos!")
            for (player in currentMap!!.playersInArena) {
                player.sendMessage("${ChatColor.GRAY}+$1 por sobreviver.")

                val cp: CPlayer = NaturalDisaster.instance.cPlayerManager.getCPlayer(player.uniqueId)!!
                cp.money = cp.money + 1
            }
            teleportSpectatorToArena(p)
        }
    }

    fun endGame() {
        currentDisaster!!.stopDisaster()

        for (p in currentMap!!.playersInArena) arenaPlayerManager!!.returnPlayerToNormal(p)

        for (p in Bukkit.getOnlinePlayers()) {
            p.setDisplayName(p.name)
            p.setPlayerListName(p.name)
            p.scoreboard = Bukkit.getScoreboardManager()!!.newScoreboard
        }

        Bukkit.getScheduler().scheduleSyncDelayedTask(main, Runnable {
            for (p in Bukkit.getOnlinePlayers()) {
                p.gameMode = GameMode.ADVENTURE
                p.teleport(Location(Bukkit.getWorld("worlds/void"), 8.0, 8.0, 8.0))
                p.inventory.clear()
                healPlayer(p)
            }
        }, 20L)

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

    fun teleportSpectatorToArena(p: Player) {
        if (currentStatus == GameStatus.IN_GAME) {
            p.gameMode = GameMode.SPECTATOR
            if (!currentMap!!.playersInArena.isEmpty()) p.teleport(currentMap!!.playersInArena[0])
        }
    }

    fun endByTimeout() {
        for (p in currentMap!!.playersInArena) {
            val cp: CPlayer = NaturalDisaster.instance.cPlayerManager.getCPlayer(p.uniqueId)!!
            cp.wins = cp.wins + 1
            cp.money = cp.money + 25
            p.sendMessage(ChatColor.GOLD.toString() + "+$25 por ganhar a partida.")
        }

        Bukkit.broadcastMessage(ChatColor.YELLOW.toString() + "Acabou o tempo do mapa, todos ainda vivos ganharam")
        endGame()
    }
}
