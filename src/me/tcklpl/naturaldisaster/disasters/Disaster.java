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
    int x1, x2, y1, y2, z1, z2, minX, minZ, gapX, gapZ, top, floor;

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

    protected void updateBlockGap() {
        x1 = map.getPos1().getBlockX();
        x2 = map.getPos2().getBlockX();
        y1 = map.getPos1().getBlockY();
        y2 = map.getPos2().getBlockY();
        z1 = map.getPos1().getBlockZ();
        z2 = map.getPos2().getBlockZ();

        minX = Math.min(x1, x2);
        minZ = Math.min(z1, z2);
        top = Math.max(y1, y2);
        floor = Math.min(y1, y2);
        gapX = Math.max(x1, x2) - minX;
        gapZ = Math.max(z1, z2) - minZ;
    }

    protected void makeRain() {
        map.getWorld().setMonsterSpawnLimit(0);
        map.getWorld().setWaterAnimalSpawnLimit(0);

        map.getWorld().setStorm(true);
        map.getWorld().setWeatherDuration(600 * 20);
        map.getWorld().setThundering(true);
        map.getWorld().setThunderDuration(600 * 20); // 10 min
    }

}
