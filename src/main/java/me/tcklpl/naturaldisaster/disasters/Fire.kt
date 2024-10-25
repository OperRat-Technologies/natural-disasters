package me.tcklpl.naturaldisaster.disasters;

import me.tcklpl.naturaldisaster.util.BiomeUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class Fire extends Disaster {

    private final int calcBuffer = 50;
    private final int replacementBuffer = 200;
    private final List<Material> burnedBlocksMaterials;

    public Fire() {
        super("Fire", true, Material.FIRE_CHARGE, BiomeUtils.PrecipitationRequirements.ANYTHING);
        burnedBlocksMaterials = new ArrayList<>();
        burnedBlocksMaterials.add(Material.COAL_BLOCK);
    }

    private boolean theresBlockInY(int x, int z) {
        for (int y = map.getLowestCoordsLocation().getBlockY() + 1; y <= map.getHighestCoordsLocation().getBlockY(); y++) {
            if (map.getWorld().getBlockAt(x, y, z).getType() != Material.AIR)
                return true;
        }
        return false;
    }

    @Override
    public void startDisaster() {
        super.startDisaster();

        Random r = new Random();
        int sourceX, sourceY, sourceZ;
        List<Integer> yCandidates = new ArrayList<>();
        List<Block> currentFireSources = new ArrayList<>();
        List<Block> previousBurnedBlocks = new ArrayList<>();

        do {
            sourceX = map.getLowestCoordsLocation().getBlockX() + r.nextInt(map.getMapSize().getX());
            sourceZ = map.getLowestCoordsLocation().getBlockZ() + r.nextInt(map.getMapSize().getZ());
        } while (!theresBlockInY(sourceX, sourceZ));

        Block sourceBlock;

        for (int y = map.getLowestCoordsLocation().getBlockY(); y <= map.getHighestCoordsLocation().getBlockY(); y++) {
            if (map.getWorld().getBlockAt(sourceX, y, sourceZ).getType() != Material.AIR)
                yCandidates.add(y);
        }

        sourceY = yCandidates.get(r.nextInt(yCandidates.size()));

        sourceBlock = map.getWorld().getBlockAt(sourceX, sourceY, sourceZ);
        currentFireSources.add(sourceBlock);

        AtomicInteger timesCycled = new AtomicInteger(0);

        int taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(main, () -> {

            if (timesCycled.get() == 0)
                sourceBlock.setType(Material.COAL_BLOCK, false);

            if ((timesCycled.incrementAndGet() % 2) == 0) {

                AtomicInteger currentBlockIndex = new AtomicInteger(0);

                List<Block> bufferedBlocks = new ArrayList<>();

                int cycles = 1 + Math.floorDiv(currentFireSources.size(), calcBuffer);

                for (int currentCycle = 0; currentCycle < cycles; currentCycle++) {

                    Bukkit.getScheduler().scheduleSyncDelayedTask(main, () -> {

                        int max = Math.min(calcBuffer, currentFireSources.size() - currentBlockIndex.get());

                        for (int i = 0; i < max; i++) {

                            Block b = currentFireSources.get(currentBlockIndex.get());

                            for (BlockFace face : BlockFace.values()) {
                                Block relative = b.getRelative(face);
                                if (relative.getType() != Material.WATER && relative .getType() != Material.AIR &&
                                        !previousBurnedBlocks.contains(relative) && !bufferedBlocks.contains(relative))
                                    bufferedBlocks.add(relative);
                            }

                            if (currentBlockIndex.get() < (currentFireSources.size() - 1))
                                currentBlockIndex.incrementAndGet();
                        }

                    }, 1 + currentCycle);

                }

                // Schedule to the end of the buffers
                Bukkit.getScheduler().scheduleSyncDelayedTask(main, () -> {
                    for (Block b : bufferedBlocks) {
                        if (!previousBurnedBlocks.contains(b))
                            previousBurnedBlocks.add(b);
                    }
                    map.bufferedReplaceBlocks(bufferedBlocks, Material.MAGMA_BLOCK, replacementBuffer, false);

                    Bukkit.getScheduler().scheduleSyncDelayedTask(main, () -> map.bufferedReplaceBlocks(bufferedBlocks, burnedBlocksMaterials, replacementBuffer, false), 20L);

                    currentFireSources.clear();
                    currentFireSources.addAll(bufferedBlocks);
                }, 2 + cycles);

            }

            // Do damage to the player
            for (Player p : map.getPlayersInArena()) {

                assert p != null;
                Material m = p.getLocation().getBlock().getRelative(BlockFace.DOWN).getType();
                if (m == Material.MAGMA_BLOCK || m == Material.COAL_BLOCK || m == Material.BLACK_TERRACOTTA) {
                    p.damage(4);
                    p.setFireTicks(60);
                }
            }

        }, startDelay, 20L);

        registerTasks(taskId);

    }
}
