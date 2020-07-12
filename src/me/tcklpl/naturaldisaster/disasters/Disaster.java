package me.tcklpl.naturaldisaster.disasters;

import me.tcklpl.naturaldisaster.NaturalDisaster;
import me.tcklpl.naturaldisaster.map.ArenaBiomeType;
import me.tcklpl.naturaldisaster.map.DisasterMap;
import me.tcklpl.naturaldisaster.map.MapManager;
import me.tcklpl.naturaldisaster.reflection.ReflectionUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public abstract class Disaster {

    DisasterMap map;
    JavaPlugin main;
    String name, hint;
    int taskId, timeoutTaskId;
    boolean isActive;
    protected boolean playable;
    protected Material icon;
    protected ReflectionUtils.PrecipitationType precipitationType;
    protected ArenaBiomeType arenaBiomeType;
    protected Biome arenaSpecificBiome;

    long startDelay = 100L;
    long timeout = 3L; // minutes
    Random random;

    /**
     * Constructor to abstract disaster class, to be used inside children.
     * @param map initially null, to be setted later.
     * @param main main reference needed to schedule tasks.
     */
    public Disaster(DisasterMap map, JavaPlugin main) {
        this.map = map;
        this.main = main;
        isActive = false;
        random = new Random();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Disaster disaster = (Disaster) o;
        return Objects.equals(name, disaster.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    /**
     * General method to start disaster, children inherit this method to make the disasters.
     * Method also schedules a task to timeout the arena.
     */
    public void startDisaster() {
        isActive = true;
        timeoutTaskId = Bukkit.getScheduler().scheduleSyncDelayedTask(main, this::endByTimeout, timeout * 20 * 60);
    }

    /**
     * Private method to end by timeout, to be called by the scheduled task declared above.
     */
    private void endByTimeout() {
        isActive = false;
        Bukkit.getScheduler().cancelTask(taskId);
        NaturalDisaster.getMapManager().arenaTimeout();
    }

    /**
     * Method to be called when the game ends, it cancells (if active) the disaster main task and the timeout task.
     */
    public void stopDisaster() {
        if (isActive) {
            Bukkit.getScheduler().cancelTask(taskId);
            Bukkit.getScheduler().cancelTask(timeoutTaskId);
        }
        isActive = false;
    }

    public String getName() { return name; }

    public void setMap(DisasterMap map) {
        this.map = map;
        switch (arenaBiomeType) {
            case SPECIFIC:
                map.setArenaBiome(arenaSpecificBiome);
                break;
            case RANDOM_PER_PRECIPITATION:
                map.setArenaRandomBiomeBasedOnPrecipitationType(precipitationType);
                break;
        }
    }

    public String getHint() { return hint; }

    public boolean isPlayable() {
        return playable;
    }

    public Material getIcon() {
        return icon;
    }
}
