package me.tcklpl.naturaldisaster;

import me.tcklpl.naturaldisaster.commands.*;
import me.tcklpl.naturaldisaster.events.Death;
import me.tcklpl.naturaldisaster.events.Explosion;
import me.tcklpl.naturaldisaster.events.Join;
import me.tcklpl.naturaldisaster.events.Motd;
import me.tcklpl.naturaldisaster.map.DisasterMap;
import me.tcklpl.naturaldisaster.map.MapManager;
import me.tcklpl.naturaldisaster.worlds.WorldManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;

public class Main extends JavaPlugin {

    GameStatus currentStatus;
    WorldManager worldManager;

    @Override
    public void onEnable() {

        currentStatus = GameStatus.PREPARING;

        List<String> managedWorlds = getConfig().getStringList("worlds");
        worldManager = new WorldManager(managedWorlds);

        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new Motd(), this);
        pm.registerEvents(new Join(), this);
        //pm.registerEvents(new Explosion(), this);
        pm.registerEvents(new Death(this), this);

        if (getConfig().getConfigurationSection("arenas") != null)
        for (String arena : getConfig().getConfigurationSection("arenas").getKeys(false)) {
            String path = "arenas." + arena;
            String world = getConfig().getString(path + ".world");
            int pos1X = getConfig().getInt(path + ".pos1.x");
            int pos1Y = getConfig().getInt(path + ".pos1.y");
            int pos1Z = getConfig().getInt(path + ".pos1.z");
            int pos2X = getConfig().getInt(path + ".pos2.x");
            int pos2Y = getConfig().getInt(path + ".pos2.y");
            int pos2Z = getConfig().getInt(path + ".pos2.z");

            World w = getServer().createWorld(new WorldCreator(world));
            assert w != null;
            w.setAutoSave(false);

            Location pos1 = new Location(w, pos1X, pos1Y, pos1Z);
            Location pos2 = new Location(w, pos2X, pos2Y, pos2Z);
            List<Location> spawns = new ArrayList<>();

            for (String spawnCode : getConfig().getConfigurationSection(path + ".spawns").getKeys(false)) {
                String spawnName = path + ".spawns." + spawnCode;
                int spawnX = getConfig().getInt(spawnName + ".x");
                int spawnY = getConfig().getInt(spawnName + ".y");
                int spawnZ = getConfig().getInt(spawnName + ".z");
                Location spawnLoc = new Location(w, spawnX, spawnY, spawnZ);
                spawns.add(spawnLoc);
            }

            DisasterMap map = new DisasterMap(pos1, pos2, arena, spawns);
            MapManager.registerArena(map);
            getLogger().log(Level.FINE, "Loaded arena " + arena);

            //getServer().unloadWorld(w, false);
        }

        Objects.requireNonNull(getCommand("world")).setExecutor(new WorldCommands(worldManager));
        GamemodeCommands gmc = new GamemodeCommands();
        Objects.requireNonNull(getCommand("creative")).setExecutor(gmc);
        Objects.requireNonNull(getCommand("survival")).setExecutor(gmc);
        Objects.requireNonNull(getCommand("adventure")).setExecutor(gmc);
        Objects.requireNonNull(getCommand("spectator")).setExecutor(gmc);
        Objects.requireNonNull(getCommand("arena")).setExecutor(new MapCreator());
        //Objects.requireNonNull(getCommand("tntrain")).setExecutor(new TntRain(this));
        Objects.requireNonNull(getCommand("teststart")).setExecutor(new GameTest(this));
    }

    @Override
    public void onDisable() {

        getConfig().set("worlds", worldManager.getManagedWorlds());

        for (DisasterMap map : MapManager.getAllArenas()) {
            try {
                getConfig().set("arenas." + map.getName() + ".world", map.getPos1().getWorld().getName());
                getConfig().set("arenas." + map.getName() + ".pos1.x", map.getPos1().getBlockX());
                getConfig().set("arenas." + map.getName() + ".pos1.y", map.getPos1().getBlockY());
                getConfig().set("arenas." + map.getName() + ".pos1.z", map.getPos1().getBlockZ());
                getConfig().set("arenas." + map.getName() + ".pos2.x", map.getPos2().getBlockX());
                getConfig().set("arenas." + map.getName() + ".pos2.y", map.getPos2().getBlockY());
                getConfig().set("arenas." + map.getName() + ".pos2.z", map.getPos2().getBlockZ());

                int count = 0;
                for (Location loc : map.getSpawns()) {
                    getConfig().set("arenas." + map.getName() + ".spawns.spawn" + count + ".x", loc.getBlockX());
                    getConfig().set("arenas." + map.getName() + ".spawns.spawn" + count + ".y", loc.getBlockY());
                    getConfig().set("arenas." + map.getName() + ".spawns.spawn" + count + ".z", loc.getBlockZ());
                    count++;
                }
            } catch (NullPointerException e) {
                getLogger().log(Level.WARNING, "Erro ao salvar arena " + map.getName() + ", mapa provavelmente não carregado");
            }


        }

        saveConfig();

    }
}
