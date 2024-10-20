package me.tcklpl.naturaldisaster.disasters;

import me.tcklpl.naturaldisaster.map.DisasterMap;
import me.tcklpl.naturaldisaster.util.BiomeUtils;
import me.tcklpl.naturaldisaster.util.EarthquakeUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class Earthquake extends Disaster {

    private record FissureColumnBlocks(List<Block> blocksToFall, List<Block> blocksToErase) {}

    public Earthquake() {
        super("Earthquake", true, Material.COBBLESTONE, BiomeUtils.PrecipitationRequirements.ANYTHING);
    }

    private FissureColumnBlocks getYColumnBlocks(int x, int z) {

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

        return new FissureColumnBlocks(blocksToBreak, blocksToDisappear);
    }


    private void destroyCrackPattern(List<Block> blocks) {
        List<Block> blocksToFall = new ArrayList<>();
        List<Block> blocksToDisappear = new ArrayList<>();
        for (Block b : blocks) {
            FissureColumnBlocks candidates = getYColumnBlocks(b.getX(), b.getZ());
            blocksToFall.addAll(candidates.blocksToFall());
            blocksToDisappear.addAll(candidates.blocksToErase());
        }

        map.bufferedReplaceBlocks(blocksToDisappear, Material.AIR, 300, false);
        map.bufferedReplaceBlocks(blocksToFall, Material.AIR, 300, true);
    }

    @Override
    public void startDisaster() {
        super.startDisaster();
        map.setPrecipitation(DisasterMap.MapPrecipitation.RANDOM);

        var crackDirections = EarthquakeUtils.randomizeCrackDirection();
        var crack = new ArrayList<Block>();

        int sizeX = map.getHighestCoordsLocation().getBlockX() - map.getLowestCoordsLocation().getBlockX();
        int sizeZ = map.getHighestCoordsLocation().getBlockZ() - map.getLowestCoordsLocation().getBlockZ();

        // Generate somewhere in the center of the map
        int generatorX = map.getLowestCoordsLocation().getBlockX() + (sizeX / 3) + Math.round(random.nextFloat() * ((float) sizeX / 3));
        int generatorZ = map.getLowestCoordsLocation().getBlockZ() + (sizeZ / 3) + Math.round(random.nextFloat() * ((float) sizeZ / 3));

        // Randomize start of the crack somewhere in the center of the map
        Block generator = map.getWorld().getBlockAt(generatorX, map.getMinY(), generatorZ);
        crack.add(generator);

        // Expand the crack on 2 directions until the end of the map
        EarthquakeUtils.expandCrackOnDirection(generator, crackDirections[0], map.getLowestCoordsLocation(), map.getHighestCoordsLocation(), crack);
        EarthquakeUtils.expandCrackOnDirection(generator, crackDirections[1], map.getLowestCoordsLocation(), map.getHighestCoordsLocation(), crack);

        // Pre-calculate fissure expansions based on the initial fissure
        var expansions = EarthquakeUtils.getCrackExpansions(crack, map.getLowestCoordsLocation(), map.getHighestCoordsLocation(), crackDirections);

        // Task to try to expand the fissure every 3 seconds
        AtomicInteger currentExpansion = new AtomicInteger(0);
        int fissureExpansionTask = Bukkit.getScheduler().scheduleSyncRepeatingTask(main, () -> {

            // 50% of chance
            if (random.nextInt(2) == 0) {
                if (currentExpansion.get() < expansions.size()) {
                    var index = currentExpansion.getAndIncrement();
                    destroyCrackPattern(expansions.get(index));
                }
            }

        }, startDelay, 3 * 20);

        registerTasks(fissureExpansionTask);
    }
}
