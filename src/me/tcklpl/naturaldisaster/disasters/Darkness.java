package me.tcklpl.naturaldisaster.disasters;

import me.tcklpl.naturaldisaster.map.ArenaBiomeType;
import me.tcklpl.naturaldisaster.map.DisasterMap;
import me.tcklpl.naturaldisaster.reflection.ReflectionUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Darkness extends Disaster {

    public Darkness(DisasterMap map, JavaPlugin main) {
        super(map, main);
        name = "Darkness";
        hint = "Te fode ae kkk";
        playable = true;
        icon = Material.BLACK_CONCRETE;
        precipitationType = ReflectionUtils.PrecipitationType.ALL;
        arenaBiomeType = ArenaBiomeType.RANDOM_PER_PRECIPITATION;
    }

    private List<Block> getAllLightSourceBlocks() {
        List<Block> lightSources = new ArrayList<>();
        for (int x = map.minX; x <= map.minX + map.gapX; x++) {
            for (int z = map.minZ; z <= map.minZ + map.gapZ; z++) {
                for (int y = map.floor; y <= map.top; y++) {
                    Block b = map.getWorld().getBlockAt(x, y, z);
                    if (b.getLightFromBlocks() >= 14 && !b.isEmpty()) {
                        lightSources.add(b);
                    }
                }
            }
        }
        return lightSources;
    }

    @Override
    public void startDisaster() {
        super.startDisaster();

        List<Block> lightSources = getAllLightSourceBlocks();
        Collections.shuffle(lightSources);

        int taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(main, () -> {

        }, startDelay, 20L);

        registerTasks(taskId);

    }
}
