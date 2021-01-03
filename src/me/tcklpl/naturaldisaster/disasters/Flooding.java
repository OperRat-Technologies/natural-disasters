package me.tcklpl.naturaldisaster.disasters;

import me.tcklpl.naturaldisaster.reflection.ReflectionWorldUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class Flooding extends Disaster {

    private final HashMap<Integer, List<Block>> blocksToChangePerYLevel;
    private final HashMap<Integer, List<Block>> blocksToWaterlogPerYLevel;

    public Flooding() {
        super("Flooding", true, Material.WATER_BUCKET, ReflectionWorldUtils.Precipitation.RAIN);
        blocksToChangePerYLevel = new HashMap<>();
        blocksToWaterlogPerYLevel = new HashMap<>();
    }

    private void calcBlocksToChange() {
        for (int y = map.floor; y <= map.top; y++) {
            List<Block> change = new ArrayList<>(), waterlog = new ArrayList<>();
            for (int x = map.minX; x <= map.minX + map.gapX; x++) {
                for (int z = map.minZ; z <= map.minZ + map.gapZ; z++) {
                    Block b = map.getWorld().getBlockAt(x, y, z);
                    if ((b.getType() == Material.AIR || !b.getType().isSolid() || !b.getType().isOccluding()) && !(b.getBlockData() instanceof Waterlogged))
                        change.add(b);
                    if (b.getBlockData() instanceof Waterlogged) {
                        waterlog.add(b);
                    }
                }
            }
            blocksToChangePerYLevel.put(y, change);
            blocksToWaterlogPerYLevel.put(y, waterlog);
        }
    }

    private void floodYLevel(int y) {

        map.bufferedReplaceBlocks(blocksToChangePerYLevel.get(y), Material.WATER, 500, false);
        for (Block b : blocksToWaterlogPerYLevel.get(y)) {
            Waterlogged waterlogged = (Waterlogged) b.getBlockData();
            waterlogged.setWaterlogged(true);
            b.setBlockData(waterlogged);
        }
    }

    @Override
    public void startDisaster() {
        super.startDisaster();

        calcBlocksToChange();
        map.makeRain(false);

        AtomicInteger currentY = new AtomicInteger(map.floor);

        // 5s
        int waterRiseTask = Bukkit.getScheduler().scheduleSyncRepeatingTask(main, () -> {
            if (currentY.get() <= map.top) {
                floodYLevel(currentY.get());
                currentY.getAndIncrement();
            }
        }, startDelay, 100L);

        // 0.5s
        int damageTask = Bukkit.getScheduler().scheduleSyncRepeatingTask(main, () -> {
            // Damage players that are on water
            if (map.getPlayersInArena().size() > 0)
                for (Player p : map.getPlayersInArena()) {
                    assert p != null;
                    if (p.getLocation().getY() < currentY.get())
                        p.damage(1);
                }
            map.damagePlayerOutsideBounds(3);
        }, startDelay, 10L);

        registerTasks(waterRiseTask, damageTask);
    }
}
