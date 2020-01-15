package me.tcklpl.naturaldisaster;

import me.tcklpl.naturaldisaster.commands.*;
import me.tcklpl.naturaldisaster.events.*;
import me.tcklpl.naturaldisaster.map.MapManager;
import me.tcklpl.naturaldisaster.worlds.WorldManager;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.Objects;

public class Main extends JavaPlugin {

    GameStatus currentStatus;
    WorldManager worldManager;

    @Override
    public void onEnable() {

        currentStatus = GameStatus.PREPARING;

        List<String> managedWorlds = getConfig().getStringList("worlds");
        worldManager = new WorldManager(managedWorlds);

        MapManager.getInstance().setMainReference(this);
        MapManager.getInstance().setupDisasters();
        MapManager.getInstance().setupArenas();

        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new Motd(), this);
        pm.registerEvents(new Join(), this);
        pm.registerEvents(new Death(), this);
        pm.registerEvents(new PickItem(), this);

        Objects.requireNonNull(getCommand("world")).setExecutor(new WorldCommands(worldManager));
        GamemodeCommands gmc = new GamemodeCommands();
        Objects.requireNonNull(getCommand("creative")).setExecutor(gmc);
        Objects.requireNonNull(getCommand("survival")).setExecutor(gmc);
        Objects.requireNonNull(getCommand("adventure")).setExecutor(gmc);
        Objects.requireNonNull(getCommand("spectator")).setExecutor(gmc);
        Objects.requireNonNull(getCommand("arena")).setExecutor(new MapCreator());
        Objects.requireNonNull(getCommand("start")).setExecutor(new Start(this));
    }

    @Override
    public void onDisable() {

        getConfig().set("worlds", worldManager.getManagedWorlds());
        MapManager.getInstance().saveArenas();

        saveConfig();

    }
}
