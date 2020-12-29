package me.tcklpl.naturaldisaster.disasters;

import me.tcklpl.naturaldisaster.map.ArenaBiomeType;
import me.tcklpl.naturaldisaster.map.DisasterMap;
import me.tcklpl.naturaldisaster.reflection.ReflectionUtils;
import me.tcklpl.naturaldisaster.util.CollectionUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

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
                        String name = b.getBlockData().getMaterial().toString().toLowerCase();
                        if (name.contains("torch") || name.contains("beacon") || name.contains("lantern")
                                || name.contains("fire") || name.contains("glow") || name.contains("lamp")
                                || name.contains("portal") || name.contains("magma"))
                            lightSources.add(b);
                        if (name.contains("lava") && !lightSources.contains(b))
                            lightSources.addAll(map.expandAndGetAllBlocksFromType(b, 30));
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

        int numberOfBlocksPerCycle = lightSources.size() / 8 + random.nextInt(6);

        List<List<Block>> sublistsOfLightSources = CollectionUtils.chopped(lightSources, numberOfBlocksPerCycle);

        map.progressivelyAdvanceTime(18000);
        PotionEffect blindness = new PotionEffect(PotionEffectType.BLINDNESS, 20, 2);

        int removeLightsTask = Bukkit.getScheduler().scheduleSyncRepeatingTask(main, () -> {

            if (!sublistsOfLightSources.isEmpty()) {
                List<Block> lightSourcesToDelete = sublistsOfLightSources.get(0);
                map.fastReplaceBlocks(lightSourcesToDelete, Material.AIR, false);
                lightSourcesToDelete.forEach( b -> {
                        for (Player p : map.getPlayersInArena())
                            if (b.getLocation().distanceSquared(p.getLocation()) <= 16) {
                                p.addPotionEffect(blindness);
                            }
                        Objects.requireNonNull(b.getLocation().getWorld())
                                .playSound(b.getLocation(), Sound.BLOCK_BONE_BLOCK_BREAK, 10, 1);
                    }
                );
                sublistsOfLightSources.remove(0);
            }
        }, startDelay, 80L);

        int damageTask = Bukkit.getScheduler().scheduleSyncRepeatingTask(main, () -> {
            if (map != null)
                for (Player p : map.getPlayersInArena()) {
                    if (p.getLocation().getBlock().getLightFromBlocks() <= 6)
                        p.damage(2);
                }
        }, startDelay, 5L);

        int spawnMobsTask = Bukkit.getScheduler().scheduleSyncRepeatingTask(main, () -> {
            if (random.nextInt(3) == 1) {
                if (map != null)
                    for (Player p : map.getPlayersInArena()) {
                        if (p.getLocation().getBlock().getLightFromBlocks() <= 6) {
                            Enderman e = map.getWorld().spawn(p.getLocation().add(-3 + random.nextInt(6),
                                    0, -3 + random.nextInt(6)), Enderman.class);
                            e.setHealth(1);
                            e.setTarget(p);
                        }
                    }
            }
        }, startDelay, 60L);

        registerTasks(removeLightsTask, damageTask, spawnMobsTask);

    }
}
