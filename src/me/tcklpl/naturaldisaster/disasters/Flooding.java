package me.tcklpl.naturaldisaster.disasters;

import me.tcklpl.naturaldisaster.map.ArenaBiomeType;
import me.tcklpl.naturaldisaster.map.DisasterMap;
import me.tcklpl.naturaldisaster.reflection.ReflectionUtils;
import me.tcklpl.naturaldisaster.reflection.ReflectionWorldUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class Flooding extends Disaster {

    private final HashMap<Integer, List<Block>> blocksToChangePerYLevel;
    private final HashMap<Integer, List<Block>> blocksToWaterlogPerYLevel;

    public Flooding(DisasterMap map, JavaPlugin main) {
        super(map, main);
        name = "Flooding";
        hint = "Procure locais altos.";
        playable = true;
        icon = Material.WATER_BUCKET;
        precipitationType = ReflectionUtils.PrecipitationType.RAIN;
        arenaBiomeType = ArenaBiomeType.RANDOM_PER_PRECIPITATION;
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

        for (Block b : blocksToWaterlogPerYLevel.get(y)) {
            Waterlogged waterlogged = (Waterlogged) b.getBlockData();
            waterlogged.setWaterlogged(true);
            b.setBlockData(waterlogged);
        }
        //map.bufferedReplaceBlocks(blocksToChangePerYLevel.get(y), Material.WATER, 500, false);
        try {
            ReflectionWorldUtils.replaceBlocksWithMaterialAndUpdateForPlayers(blocksToChangePerYLevel.get(y), Material.WATER, false, map.getPlayersInArena());
        } catch (Exception e) {
            e.printStackTrace();
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
                    if (p.getLocation().getY() <= currentY.get())
                        p.damage(1);
                }
            map.damagePlayerOutsideBounds(3);
        }, startDelay, 10L);

        registerTasks(waterRiseTask, damageTask);
    }
}
