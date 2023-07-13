package me.tcklpl.naturaldisaster.disasters;

import com.google.common.collect.Lists;
import me.tcklpl.naturaldisaster.NaturalDisaster;
import me.tcklpl.naturaldisaster.util.BiomeUtils;
import me.tcklpl.naturaldisaster.util.EarthquakeUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class Earthquake extends Disaster {

    private final int gravityBuffer = 200;

    /**
     * Cass GravityCandidates, to be used within Earthquake disaster.
     * HighPriorityBlocks are those on top of air or passable blocks.
     * LowPriorityBlocks are those on top of high priority blocks.
     */
    private record GravityCandidates(List<Block> highPriorityBlocks, List<Block> lowPriorityBlocks) {

    }

    public Earthquake() {
        super("Earthquake", true, Material.COBBLESTONE, BiomeUtils.PrecipitationRequirements.ANYTHING);
    }

    private GravityCandidates getYColumnBlocks(int x, int z) {

        List<Block> blocksToBreak = new ArrayList<>();
        List<Block> blocksToDisappear = new ArrayList<>();

        for (int y = map.getHighestCoordsLocation().getBlockY(); y >= map.getLowestCoordsLocation().getBlockY(); y--) {
            Block b = map.getWorld().getBlockAt(x, y, z);
            if (b.getType() == Material.AIR) continue;

            if (b.isLiquid() ||
                    b.isPassable() ||
                    b.getType().toString().contains("LEAVES") ||                                              // leaves will disappear
                    b.getType().toString().contains("DOOR") ||                                                // doors and trapdoors too
                    (!b.getRelative(BlockFace.UP).isEmpty() && b.getRelative(BlockFace.UP, 2).isEmpty()) || // blocks with another one on top (below floor) too
                    (blocksToDisappear.contains(b.getRelative(BlockFace.UP)))                                  // stacked blocks
            )
                blocksToDisappear.add(b);
            else blocksToBreak.add(b);

        }

        return new GravityCandidates(blocksToBreak, blocksToDisappear);
    }

    private GravityCandidates generateGravityCandidates() {

        List<Block> gravityCandidates = new ArrayList<>();
        List<Block> lowPriorityGravityCandidates = new ArrayList<>();

        Random r = new Random();
        // Blocos podem ser afetados por gravidade acima de 1/2 da altura do mapa + [0, 1/4 da altura do mapa]
        int yTreshold = Math.floorDiv(map.getMapSize().getY(), 2) + r.nextInt(Math.floorDiv(map.getMapSize().getY(), 4));

        for (int y = yTreshold; y <= map.getHighestCoordsLocation().getBlockY(); y++) {
            for (int x = map.getLowestCoordsLocation().getBlockX(); x <= map.getHighestCoordsLocation().getBlockX(); x++)
                for (int z = map.getLowestCoordsLocation().getBlockZ(); z <= map.getHighestCoordsLocation().getBlockZ(); z++)
                    if (map.getWorld().getBlockAt(x, y, z).getType() != Material.AIR) {
                        Block b = map.getWorld().getBlockAt(x, y, z);
                        Block downRelative = b.getRelative(BlockFace.DOWN);
                        if (!b.getType().toString().contains("LEAVES"))
                            if (downRelative.getType() == Material.AIR || downRelative.isPassable())
                                gravityCandidates.add(b);
                            else if (gravityCandidates.contains(downRelative))
                                lowPriorityGravityCandidates.add(b);
                    }
        }

        return new GravityCandidates(gravityCandidates, lowPriorityGravityCandidates);
    }

    private void breakGravityCandidates(GravityCandidates gravityCandidates) {

        double secondsBetweenGravityBatches = 0.8;

        Collections.shuffle(gravityCandidates.highPriorityBlocks());
        Collections.shuffle(gravityCandidates.lowPriorityBlocks());

        List<List<Block>> highPriorityBatches = Lists.partition(gravityCandidates.highPriorityBlocks(), gravityBuffer);
        List<List<Block>> lowPriorityBatches  = Lists.partition(gravityCandidates.lowPriorityBlocks() , gravityBuffer);

        for (int i = 0; i < highPriorityBatches.size(); i++) {

            int finalI = i;
            Bukkit.getScheduler().scheduleSyncDelayedTask(main,
                    () -> map.bufferedReplaceBlocks(highPriorityBatches.get(finalI), Material.AIR, gravityBuffer, true),
                    1 + Math.round(i * secondsBetweenGravityBatches * 20L));
        }

        for (int i = 0; i < lowPriorityBatches.size(); i++) {

            int finalI = i;
            Bukkit.getScheduler().scheduleSyncDelayedTask(main,
                    () -> map.bufferedReplaceBlocks(lowPriorityBatches.get(finalI), Material.AIR, gravityBuffer, true),
                    1 + ((highPriorityBatches.size() / gravityBuffer) * Math.round(secondsBetweenGravityBatches * 20)) +
                            Math.round(i * secondsBetweenGravityBatches * 20L));
        }
    }

    private int i = 0;
    private Material[] debug = new Material[] { Material.RED_WOOL, Material.YELLOW_WOOL, Material.GREEN_WOOL, Material.BLUE_WOOL, Material.PURPLE_WOOL};
    private void destroyCrackPattern(List<Block> blocks) {
        List<Block> highBlocks = new ArrayList<>();
        List<Block> lowBlocks = new ArrayList<>();
        for (Block b : blocks) {
            GravityCandidates candidates = getYColumnBlocks(b.getX(), b.getZ());
            highBlocks.addAll(candidates.highPriorityBlocks());
            lowBlocks.addAll(candidates.lowPriorityBlocks());
        }

        var mat = debug[i++ % debug.length];
        map.bufferedReplaceBlocks(lowBlocks, mat, 300, false);
        map.bufferedReplaceBlocks(highBlocks, mat, 300, true);
    }

    @Override
    public void startDisaster() {
        super.startDisaster();

        map.makeRain(true);

        Random r = NaturalDisaster.getRandom();
        var crackDirections = EarthquakeUtils.randomizeCrackDirection();
        var crack = new ArrayList<Block>();

        int sizeX = map.getHighestCoordsLocation().getBlockX() - map.getLowestCoordsLocation().getBlockX();
        int sizeZ = map.getHighestCoordsLocation().getBlockZ() - map.getLowestCoordsLocation().getBlockZ();

        // Generate somewhere in the center of the map
        int generatorX = map.getLowestCoordsLocation().getBlockX() + (sizeX / 3) + Math.round(random.nextFloat() * ((float) sizeX / 3));
        int generatorZ = map.getLowestCoordsLocation().getBlockZ() + (sizeZ / 3) + Math.round(random.nextFloat() * ((float) sizeZ / 3));

        Block generator = map.getWorld().getBlockAt(generatorX, map.getMinY(), generatorZ);
        crack.add(generator);

        EarthquakeUtils.expandCrackOnDirection(generator, crackDirections[0], map.getLowestCoordsLocation(), map.getHighestCoordsLocation(), crack);
        EarthquakeUtils.expandCrackOnDirection(generator, crackDirections[1], map.getLowestCoordsLocation(), map.getHighestCoordsLocation(), crack);

        NaturalDisaster.getMainReference().getLogger().info("Crack size: " + crack.size());

        var expansions = EarthquakeUtils.getCrackExpansions(crack, map.getLowestCoordsLocation(), map.getHighestCoordsLocation(), crackDirections);

        AtomicInteger timesRunned = new AtomicInteger(0);
        AtomicInteger currentExpansion = new AtomicInteger(0);
        AtomicBoolean hasCracked = new AtomicBoolean(false);

        int taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(main, () -> {

            timesRunned.addAndGet(1);
            if ((timesRunned.get() % 3) == 0) {

                if (currentExpansion.get() < expansions.size()) {
                    var index = currentExpansion.getAndIncrement();
                    destroyCrackPattern(expansions.get(index));
                }


//                // 40% de chance
//                if (r.nextInt(100) < 70) {
//
//
//
////                    if (hasCracked.get()) {
////                        destroyCrackPattern(expansion);
////                    } else {
////                        hasCracked.set(true);
////                        destroyCrackPattern(initialCrack);
////
////                        breakGravityCandidates(gravityCandidates);
////
////                    }
//                }

            }


        }, startDelay, 20L);

        registerTasks(taskId);
    }
}
