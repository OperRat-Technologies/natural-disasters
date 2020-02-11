package me.tcklpl.naturaldisaster.worlds;

import me.tcklpl.naturaldisaster.NaturalDisaster;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.WorldCreator;
import org.bukkit.craftbukkit.libs.org.apache.commons.io.FileUtils;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class WorldManager {

    List<String> managedWorlds;
    List<String> loadedWorlds;

    public WorldManager(List<String> worlds) {
        managedWorlds = new ArrayList<>();
        loadedWorlds = new ArrayList<>();
        for(String world : worlds) {
            File folder = new File(Bukkit.getServer().getWorldContainer(), world);
            if (!folder.exists()) {
                NaturalDisaster.getMainReference().getLogger().log(Level.WARNING, "Could not load world " + world + ", exiting world load");
                return;
            } else {
                managedWorlds.add(world);
            }
        }
        NaturalDisaster.getMainReference().getLogger().info("Carregados " + managedWorlds.size() + " mundos");
    }

    public boolean createVoidWorld(String name) {
        if (managedWorlds.stream().noneMatch(name::equalsIgnoreCase)) {
            String voidSource = Bukkit.getServer().getWorldContainer() + "/void-template";
            File srcDir = new File(voidSource);
            String voidDest = Bukkit.getServer().getWorldContainer() + "/" + name;
            File destDir = new File(voidDest);
            try {
                FileUtils.copyDirectory(srcDir, destDir);
            } catch (IOException e) {
                e.printStackTrace();
            }
            managedWorlds.add(name);
            return true;
        }
        return false;
    }

    public boolean teleportPlayer(Player p, String world) {
        if (managedWorlds.stream().anyMatch(world::equalsIgnoreCase)) {
            if (loadedWorlds.stream().noneMatch(world::equalsIgnoreCase)) {
                Bukkit.getServer().createWorld(new WorldCreator(world));
                loadedWorlds.add(world);
            }
            Location loc = new Location(Bukkit.getWorld(world), 0, 100, 0);
            p.teleport(loc);
            return true;
        }
        return false;
    }

    public List<String> getManagedWorlds() {
        return managedWorlds;
    }



}
