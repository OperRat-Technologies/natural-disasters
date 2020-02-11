package me.tcklpl.naturaldisaster.disasters;

import me.tcklpl.naturaldisaster.map.DisasterMap;
import me.tcklpl.naturaldisaster.map.MapManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Random;

public abstract class Disaster {

    DisasterMap map;
    JavaPlugin main;
    String name, hint;
    int taskId, timeoutTaskId;
    boolean isActive;

    long startDelay = 100L;
    long timeout = 3L; // minutes
    Random random;

    public Disaster(DisasterMap map, JavaPlugin main) {
        this.map = map;
        this.main = main;
        isActive = false;
        random = new Random();
    }

    public void startDisaster() {
        isActive = true;
        timeoutTaskId = Bukkit.getScheduler().scheduleSyncDelayedTask(main, this::endByTimeout, timeout * 20 * 60);
    }

    private void endByTimeout() {
        isActive = false;
        Bukkit.getScheduler().cancelTask(taskId);
        MapManager.getInstance().arenaTimeout();
    }

    public void stopDisaster() {
        if (isActive) {
            Bukkit.getScheduler().cancelTask(taskId);
            Bukkit.getScheduler().cancelTask(timeoutTaskId);
        }
        isActive = false;
    }

    public String getName() { return name; }

    public void setMap(DisasterMap map) { this.map = map; }

    public String getHint() { return hint; }

}
