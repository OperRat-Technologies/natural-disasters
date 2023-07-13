package me.tcklpl.naturaldisaster.util;

import me.tcklpl.naturaldisaster.NaturalDisaster;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EarthquakeUtils {

    private static final Random random = NaturalDisaster.getRandom();
    private static final BlockFace[] crackDirections = new BlockFace[] {
        BlockFace.NORTH, BlockFace.NORTH_EAST, BlockFace.EAST, BlockFace.SOUTH_EAST, BlockFace.SOUTH, BlockFace.SOUTH_WEST, BlockFace.WEST, BlockFace.NORTH_WEST
    };

    private static final Map<BlockFace, BlockFace[]> crackDirectionExpansions = Map.ofEntries(
        Map.entry(BlockFace.NORTH, new BlockFace[] { BlockFace.NORTH_WEST, BlockFace.NORTH, BlockFace.NORTH_EAST }),
        Map.entry(BlockFace.NORTH_EAST, new BlockFace[] { BlockFace.NORTH, BlockFace.NORTH_EAST, BlockFace.EAST }),
        Map.entry(BlockFace.EAST, new BlockFace[] { BlockFace.NORTH_EAST, BlockFace.EAST, BlockFace.SOUTH_EAST }),
        Map.entry(BlockFace.SOUTH_EAST, new BlockFace[] { BlockFace.EAST, BlockFace.SOUTH_EAST, BlockFace.SOUTH }),
        Map.entry(BlockFace.SOUTH, new BlockFace[] { BlockFace.SOUTH_EAST, BlockFace.SOUTH, BlockFace.SOUTH_WEST }),
        Map.entry(BlockFace.SOUTH_WEST, new BlockFace[] { BlockFace.SOUTH, BlockFace.SOUTH_WEST, BlockFace.WEST }),
        Map.entry(BlockFace.WEST, new BlockFace[] { BlockFace.SOUTH_WEST, BlockFace.WEST, BlockFace.NORTH_WEST }),
        Map.entry(BlockFace.NORTH_WEST, new BlockFace[] { BlockFace.WEST, BlockFace.NORTH_WEST, BlockFace.NORTH })
    );

    private static List<BlockFace> getCrackDirectionExpansions(BlockFace direction) {
        int index = Arrays.stream(crackDirections).toList().indexOf(direction);
        return Stream.of(-2, -1, 0, 1, 2)
                .map(i -> index + i)                                // add all offsets to the current index
                .map(i -> i < 0 ? crackDirections.length + i : i)   // deal with negative indices
                .map(i -> i % crackDirections.length)               // deal with indices bigger than the length
                .map(i -> crackDirections[i])
                .collect(Collectors.toList());
    }

    public static BlockFace[] randomizeCrackDirection() {
        var mainIndex = random.nextInt(crackDirections.length);
        var oppositeIndex = (mainIndex + 4) % crackDirections.length;
        return new BlockFace[] { crackDirections[mainIndex], crackDirections[oppositeIndex] };
    }

    public static void expandCrackOnDirection(Block current, BlockFace direction, Location bound1, Location bound2, List<Block> list) {
        if (!LocationUtils.isBetween(current.getLocation(), bound1, bound2)) return;

        var crackDirection = crackDirectionExpansions.get(direction)[random.nextInt(3)];
        var nextBlock = current.getRelative(crackDirection);
        list.add(nextBlock);
        expandCrackOnDirection(nextBlock, direction, bound1, bound2, list);
    }

    public static BlockFace[] getComplementaryDirections(BlockFace[] directions) {
        var mainDirectionIndex = Arrays.asList(crackDirections).indexOf(directions[0]);
        var secondaryIndex = (mainDirectionIndex + 2) % crackDirections.length;
        return new BlockFace[] { crackDirections[mainDirectionIndex], crackDirections[secondaryIndex] };
    }

    public static List<List<Block>> getCrackExpansions(List<Block> initialCrack, Location bound1, Location bound2, BlockFace[] direction) {
        var complementary = getComplementaryDirections(direction);

        var directions1 = getCrackDirectionExpansions(complementary[0]);
        var directions2 = getCrackDirectionExpansions(complementary[1]);

        List<List<Block>> expansions = new ArrayList<>();
        expansions.add(initialCrack);

        List<Block> previousIterationDir1 = initialCrack;
        List<Block> previousIterationDir2 = initialCrack;

        var alreadyComputedBlocks = new HashSet<Block>();

        for (int i = 1; ; i++) {

            // expand on one side
            List<Block> finalPreviousIterationDir1 = previousIterationDir1;
            List<Block> finalPreviousIterationDir2 = previousIterationDir2;

            var currentDir1 = directions1.stream()
                    .flatMap(dir ->
                            finalPreviousIterationDir1.stream()
                                    .map(block -> block.getRelative(dir))
                    )
                    .distinct()
                    .filter(block -> !alreadyComputedBlocks.contains(block))
                    .filter(block -> LocationUtils.isBetween(block.getLocation(), bound1, bound2))
                    .toList();

            // and on the other

            var currentDir2 = directions2.stream()
                    .flatMap(dir ->
                            finalPreviousIterationDir2.stream()
                                    .map(block -> block.getRelative(dir))
                    )
                    .distinct()
                    .filter(block -> !alreadyComputedBlocks.contains(block))
                    .filter(block -> LocationUtils.isBetween(block.getLocation(), bound1, bound2))
                    .toList();

            if (currentDir1.isEmpty() && currentDir2.isEmpty()) break;

            previousIterationDir1 = currentDir1;
            previousIterationDir2 = currentDir2;

            alreadyComputedBlocks.addAll(currentDir1);
            alreadyComputedBlocks.addAll(currentDir2);

            expansions.add(Stream.concat(currentDir1.stream(), currentDir2.stream()).toList());
        }

        return expansions;
    }

}
