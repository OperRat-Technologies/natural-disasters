package me.tcklpl.naturaldisaster.disasters;

import me.tcklpl.naturaldisaster.NaturalDisaster;
import me.tcklpl.naturaldisaster.map.DisasterMap;
import me.tcklpl.naturaldisaster.util.BiomeUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public abstract class Disaster {

    DisasterMap map;
    JavaPlugin main;
    String name;
    private final List<Integer> tasks;
    boolean isActive;
    protected boolean playable;
    protected Material icon;
    protected BiomeUtils.PrecipitationRequirements precipitationRequirements;
    protected Biome arenaSpecificBiome;

    long startDelay = 100L;
    long timeout = 3L; // minutes
    Random random;

    /**
     * Distaster contructor, to be used inside children.
     * @param name the name of the disaster.
     * @param playable if it's currently playable or is still in development.
     * @param icon the org.bukkit.Material that represents the disaster.
     * @param precipitationRequirements the precipitation that can occur on the arena.
     */
    public Disaster(String name, boolean playable, Material icon, BiomeUtils.PrecipitationRequirements precipitationRequirements) {
        this.name = name;
        this.playable = playable;
        this.icon = icon;
        this.precipitationRequirements = precipitationRequirements;

        this.main = NaturalDisaster.getMainReference();
        this.map = null;
        this.tasks = new ArrayList<>();
        this.random = new Random();
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

    public void setupDisaster() {
        int timeoutTaskId = Bukkit.getScheduler().scheduleSyncDelayedTask(main, this::endByTimeout, timeout * 20 * 60);
        int damagePlayersId = Bukkit.getScheduler().scheduleSyncRepeatingTask(main, this::damagePlayersOutsideBounds, 0L, 20L);
        registerTasks(timeoutTaskId, damagePlayersId);
    }

    /**
     * General method to start disaster, children inherit this method to make the disasters.
     * Method also schedules a task to timeout the arena.
     */
    public void startDisaster() {
        isActive = true;
    }

    private void damagePlayersOutsideBounds() {
        map.getPlayersInArena().forEach(p -> {
            var l = p.getLocation();
            if (
                l.getX() < map.getMinX() ||
                l.getY() < map.getMinY() ||
                l.getZ() < map.getMinZ() ||
                l.getX() > map.getMaxX() ||
                l.getY() > map.getMaxY() ||
                l.getZ() > map.getMaxZ()
            ) {
                p.damage(2);
            }
        });
    }

    /**
     * Private method to end by timeout, to be called by the scheduled task declared above.
     */
    private void endByTimeout() {
        stopDisaster();
        NaturalDisaster.getGameManager().endByTimeout();
    }

    public void registerTasks(int... taskNumber) {
        Arrays.stream(taskNumber).forEach(tasks::add);
    }

    /**
     * Method to be called when the game ends, it cancells all the disaster tasks.
     */
    public void stopDisaster() {
        tasks.forEach(Bukkit.getScheduler()::cancelTask);
        isActive = false;
    }

    public String getName() { return name; }

    public void setMap(DisasterMap map) {
        this.map = map;
    }

    public boolean isPlayable() {
        return playable;
    }

    public Material getIcon() {
        return icon;
    }

    public BiomeUtils.PrecipitationRequirements getPrecipitationRequirements() {
        return precipitationRequirements;
    }

    public Biome getArenaSpecificBiome() {
        return arenaSpecificBiome;
    }
}
