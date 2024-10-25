package me.tcklpl.naturaldisaster

import me.tcklpl.naturaldisaster.admin.AdminInventoryClickEvent
import me.tcklpl.naturaldisaster.admin.ArenaAdminCmd
import me.tcklpl.naturaldisaster.commands.BalanceCmd
import me.tcklpl.naturaldisaster.commands.GamemodeCmd
import me.tcklpl.naturaldisaster.commands.HealCmd
import me.tcklpl.naturaldisaster.commands.MapCreator
import me.tcklpl.naturaldisaster.commands.SchematicCreator
import me.tcklpl.naturaldisaster.commands.StartCmd
import me.tcklpl.naturaldisaster.events.ChatEvent
import me.tcklpl.naturaldisaster.events.FoodLevelEvent
import me.tcklpl.naturaldisaster.events.IceMeltEvent
import me.tcklpl.naturaldisaster.events.JoinEvent
import me.tcklpl.naturaldisaster.events.LeaveEvent
import me.tcklpl.naturaldisaster.events.MobSpawnEvent
import me.tcklpl.naturaldisaster.events.MotdEvent
import me.tcklpl.naturaldisaster.events.MoveEvent
import me.tcklpl.naturaldisaster.events.PickItemEvent
import me.tcklpl.naturaldisaster.events.arena.DamageEvent
import me.tcklpl.naturaldisaster.events.arena.DeathEvent
import me.tcklpl.naturaldisaster.player.cPlayer.CPlayerManager
import me.tcklpl.naturaldisaster.player.skins.SkinManager
import me.tcklpl.naturaldisaster.schematics.SchematicManager
import me.tcklpl.naturaldisaster.shop.ShopCommand
import me.tcklpl.naturaldisaster.shop.ShopInventoryClick
import me.tcklpl.naturaldisaster.worlds.WorldCommands
import me.tcklpl.naturaldisaster.worlds.WorldManager
import org.bukkit.plugin.java.JavaPlugin

class NaturalDisaster : JavaPlugin() {

    companion object {
        lateinit var instance: NaturalDisaster
            private set;
    }

    lateinit var worldManager: WorldManager
    lateinit var cPlayerManager: CPlayerManager
    lateinit var gameManager: GameManager
    lateinit var skinManager: SkinManager
    lateinit var schematicManager: SchematicManager

    override fun onEnable() {
        instance = this

        val managedWorlds = config.getStringList("worlds")
        worldManager = WorldManager(managedWorlds)

        gameManager = GameManager(this)
        gameManager.currentStatus = GameStatus.IN_LOBBY

        skinManager = SkinManager(this)
        skinManager.setupSkins()

        cPlayerManager = CPlayerManager()
        cPlayerManager.loadPlayers()

        schematicManager = SchematicManager()

        registerEvents()
        registerCommands()
    }

    override fun onDisable() {
        config.set("worlds", worldManager.managedWorlds)
        gameManager.arenaManager.saveArenas()
        cPlayerManager.savePlayers()
        saveConfig()
        schematicManager.saveSchematics()
    }

    private fun registerEvents() {
        val pm = server.pluginManager
        pm.registerEvents(MotdEvent, this)
        pm.registerEvents(JoinEvent, this)
        pm.registerEvents(DeathEvent, this)
        pm.registerEvents(PickItemEvent, this)
        pm.registerEvents(FoodLevelEvent, this)
        pm.registerEvents(IceMeltEvent, this)
        pm.registerEvents(LeaveEvent, this)
        pm.registerEvents(MoveEvent, this)
        pm.registerEvents(AdminInventoryClickEvent, this)
        pm.registerEvents(ChatEvent, this)
        pm.registerEvents(DamageEvent, this)
        pm.registerEvents(MobSpawnEvent, this)
        pm.registerEvents(ShopInventoryClick, this)
    }

    private fun registerCommands() {
        getCommand("world")?.setExecutor(WorldCommands)
        getCommand("world")?.tabCompleter = WorldCommands

        getCommand("creative")?.setExecutor(GamemodeCmd)
        getCommand("survival")?.setExecutor(GamemodeCmd)
        getCommand("adventure")?.setExecutor(GamemodeCmd)
        getCommand("spectator")?.setExecutor(GamemodeCmd)

        getCommand("arena")?.setExecutor(MapCreator)
        getCommand("arena")?.tabCompleter = MapCreator

        getCommand("start")?.setExecutor(StartCmd)

        getCommand("admin")?.setExecutor(ArenaAdminCmd)
        getCommand("admin")?.tabCompleter = ArenaAdminCmd

        getCommand("sch")?.setExecutor(SchematicCreator)
        getCommand("sch")?.tabCompleter = SchematicCreator

        getCommand("balance")?.setExecutor(BalanceCmd)
        getCommand("heal")?.setExecutor(HealCmd)
        getCommand("shop")?.setExecutor(ShopCommand)
    }

}
