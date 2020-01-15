package me.tcklpl.naturaldisaster.disasters;

import me.tcklpl.naturaldisaster.map.DisasterMap;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public abstract class Disaster {

    DisasterMap map;
    JavaPlugin main;
    String name;
    int taskId;
    boolean isActive;

    public Disaster(DisasterMap map, JavaPlugin main) {
        this.map = map;
        this.main = main;
        isActive = false;
    }

    public void startDisaster() {
        isActive = true;
    }

    public void stopDisaster() {
        if (isActive)
            Bukkit.getScheduler().cancelTask(taskId);
        isActive = false;
    }

    public String getName() { return name; }

    public boolean isActive() { return isActive; }

    public void setMap(DisasterMap map) { this.map = map; }

}
