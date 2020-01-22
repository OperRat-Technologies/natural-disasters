package me.tcklpl.naturaldisaster.disasters;

import me.tcklpl.naturaldisaster.map.DisasterMap;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class Flooding extends Disaster {

    public Flooding(DisasterMap map, JavaPlugin main) {
        super(map, main);
        name = "Flooding";
        hint = "Procure locais altos.";
    }

    @Override
    public void startDisaster() {
        super.startDisaster();

        map.setArenaBiome(Biome.PLAINS);
        map.makeRain(false);

        Random r = random;
        AtomicInteger currentY = new AtomicInteger(map.floor);
        AtomicInteger floodRiseChance = new AtomicInteger(50);
        AtomicInteger currentDamage = new AtomicInteger(1);
        AtomicInteger currentCycles = new AtomicInteger(0);
        AtomicInteger currentRandomTime = new AtomicInteger(5 + r.nextInt(6));

        taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(main, () -> {

            currentCycles.addAndGet(1);

            // Only execute each 5-10 seconds
            if ((currentCycles.get() % currentRandomTime.get()) == 0)
            if (currentY.get() <= map.top) {

                if (r.nextInt(100) <= floodRiseChance.get()) {

                    List<Block> blocksToChange = new ArrayList<>();

                    for (int x = map.minX; x <= map.minX + map.gapX; x++) {
                        for (int z = map.minZ; z <= map.minZ + map.gapZ; z++) {
                            Block b = map.getWorld().getBlockAt(x, currentY.get(), z);
                            blocksToChange.add(b);
                        }
                    }

                    map.bufferedBreakBlocks(blocksToChange, Material.WATER, 500, false);

                    currentY.getAndIncrement();
                    floodRiseChance.addAndGet(5);
                    currentRandomTime.set(5 + r.nextInt(6));

                }
            }

            // Damage players that are on water
            for (String s : map.getPlayersInArena()) {
                Player p = Bukkit.getPlayer(s);
                assert p != null;
                if (p.isSwimming() || map.getWorld().getBlockAt(p.getLocation()).getType() == Material.WATER)
                    p.damage(currentDamage.get());
            }

        }, startDelay, 20L);
    }
}
