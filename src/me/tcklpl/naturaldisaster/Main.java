package me.tcklpl.naturaldisaster;

import me.tcklpl.naturaldisaster.commands.*;
import me.tcklpl.naturaldisaster.events.*;
import me.tcklpl.naturaldisaster.map.MapManager;
import me.tcklpl.naturaldisaster.player.CustomPlayerManager;
import me.tcklpl.naturaldisaster.worlds.WorldManager;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.Objects;

public class Main extends JavaPlugin {

    WorldManager worldManager;

    @Override
    public void onEnable() {

        List<String> managedWorlds = getConfig().getStringList("worlds");
        worldManager = new WorldManager(managedWorlds);

        MapManager.getInstance().setMainReference(this);
        MapManager.getInstance().setupDisasters();
        MapManager.getInstance().setupArenas();
        MapManager.getInstance().setCurrentStatus(GameStatus.IN_LOBBY);

        CustomPlayerManager.getInstance().setMainInstance(this);
        CustomPlayerManager.getInstance().setupPlayers();

        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new Motd(), this);
        pm.registerEvents(new Join(), this);
        pm.registerEvents(new Death(), this);
        pm.registerEvents(new PickItem(), this);
        pm.registerEvents(new MobSpawn(), this);
        pm.registerEvents(new FoodLevel(), this);
        pm.registerEvents(new IceMelt(), this);
        pm.registerEvents(new Leave(), this);
        pm.registerEvents(new Move(), this);
        pm.registerEvents(new AdminInventoryClick(), this);

        Objects.requireNonNull(getCommand("world")).setExecutor(new WorldCommands(worldManager));
        GamemodeCommands gmc = new GamemodeCommands();
        Objects.requireNonNull(getCommand("creative")).setExecutor(gmc);
        Objects.requireNonNull(getCommand("survival")).setExecutor(gmc);
        Objects.requireNonNull(getCommand("adventure")).setExecutor(gmc);
        Objects.requireNonNull(getCommand("spectator")).setExecutor(gmc);
        Objects.requireNonNull(getCommand("arena")).setExecutor(new MapCreator(this));
        Objects.requireNonNull(getCommand("start")).setExecutor(new Start(this));
        Objects.requireNonNull(getCommand("balance")).setExecutor(new Balance());
        Objects.requireNonNull(getCommand("admin")).setExecutor(new ArenaAdmin());
        Objects.requireNonNull(getCommand("sch")).setExecutor(new SchematicCommand());
    }

    @Override
    public void onDisable() {

        getConfig().set("worlds", worldManager.getManagedWorlds());
        MapManager.getInstance().saveArenas();
//        try {
//            CustomPlayerManager.getInstance().savePlayers();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        saveConfig();

    }
}
