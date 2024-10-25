package me.tcklpl.naturaldisaster.disasters;

import me.tcklpl.naturaldisaster.map.DisasterMap;
import me.tcklpl.naturaldisaster.util.BiomeUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Snow;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.util.BoundingBox;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Blizzard extends Disaster {

    private HashMap<Integer, List<Block>> blocksToChangePerLevel = new HashMap<>();

    public Blizzard() {
        super("Blizzard", true, Material.SNOWBALL, BiomeUtils.PrecipitationRequirements.SHOULD_SNOW);
    }

    @Override
    public void setupDisaster() {
        super.setupDisaster();
        blocksToChangePerLevel = new HashMap<>();

        for (int y = map.getHighestCoordsLocation().getBlockY(); y >= map.getLowestCoordsLocation().getBlockY(); y--) {
            List<Block> layerBlocks = new ArrayList<>();

            for (int x = map.getLowestCoordsLocation().getBlockX(); x <= map.getHighestCoordsLocation().getBlockX(); x++) {
                for (int z = map.getLowestCoordsLocation().getBlockZ(); z <= map.getHighestCoordsLocation().getBlockZ(); z++) {
                    Block b = map.getWorld().getBlockAt(x, y, z);
                    if (b.getType() != Material.AIR && !b.isPassable() && (!(b.getState() instanceof InventoryHolder)))
                        layerBlocks.add(b);
                }
            }
            blocksToChangePerLevel.put(y, layerBlocks);
        }

        map.setPrecipitation(DisasterMap.MapPrecipitation.PRECIPITATE);
    }

    private int numPartialLayers(int currentY, int maxLayers, int minLayers) {
        float diff = maxLayers - minLayers;
        float percentage = ((float) currentY - (float) map.getMinY()) / ((float) map.getMapSize().getY());
        return Math.round(minLayers + diff * percentage);
    }

    private final List<Double> weights = Arrays.asList(0.5, 0.3, 0.2, 0.1, 0.05, 0.01, 0.005, 0.001);

    @Override
    public void startDisaster() {
        super.startDisaster();

        // -------------------------------------------------------------------------------------------------------------
        // Map freezing
        // -------------------------------------------------------------------------------------------------------------
        AtomicInteger currentY = new AtomicInteger(map.getMaxY());

        List<Material> blockPallete = Arrays.asList(Material.BLUE_ICE, Material.PACKED_ICE, Material.ICE);
        int maxPartialLayers = 8;
        int minPartialLayers = 2;

        int freezeTask = Bukkit.getScheduler().scheduleSyncRepeatingTask(main, () -> {
            if (currentY.get() >= map.getLowestCoordsLocation().getBlockY()) {
                map.bufferedReplaceBlocks(blocksToChangePerLevel.get(currentY.get()), blockPallete, 500, false);

                int layersBelowToPartiallyConvert = numPartialLayers(currentY.get(), maxPartialLayers, minPartialLayers);
                var extraBlocksToReplace = new HashSet<Block>();
                for (int i = 1; i <= layersBelowToPartiallyConvert; i++) {
                    int curLowerY = currentY.get() - i;
                    if (curLowerY < map.getMinY()) break;
                    var layerBlocks = blocksToChangePerLevel.get(curLowerY);
                    double weight = weights.get(i - 1);
                    long blocksToPick = Math.round(layerBlocks.size() * weight);

                    for (int j = 0; j < blocksToPick; j++) {
                        var pickedBlock = random.nextInt(layerBlocks.size());
                        extraBlocksToReplace.add(layerBlocks.get(pickedBlock));
                    }
                }

                map.bufferedReplaceBlocks(extraBlocksToReplace.stream().toList(), blockPallete, 500, false);
                currentY.decrementAndGet();
            }
        }, startDelay, 60L);

        // -------------------------------------------------------------------------------------------------------------
        // Snow layers increasing
        // -------------------------------------------------------------------------------------------------------------
        var blocksAddSnowLayers = new AtomicInteger(50);

        int snowTask = Bukkit.getScheduler().scheduleSyncRepeatingTask(main, () -> {
            for (int i = 0; i < blocksAddSnowLayers.get(); i++) {
                var randomLocation = map.getRandomBlockInMap();

                // if it's a solid block, get the one on top
                if (!randomLocation.getBlock().isPassable()) {
                    randomLocation.setY(randomLocation.getY() + 1);
                }

                if (!checkIfBlockCanBecomeSnowLayer(randomLocation.getBlock())) continue;

                // snow layer already? add another layer
                if (randomLocation.getBlock().getType().equals(Material.SNOW)) {
                    Snow snowBlock = (Snow) randomLocation.getBlock().getBlockData();
                    int newLayers = snowBlock.getLayers() + 1;

                    // already max layers? set as snow block
                    if (newLayers >= snowBlock.getMaximumLayers()) {
                        randomLocation.getBlock().setType(Material.SNOW_BLOCK);
                    }
                    // if not, just add another layer
                    else {
                        snowBlock.setLayers(newLayers);
                        randomLocation.getBlock().setBlockData(snowBlock);
                    }
                }
                // not a snow layer, set block as snow layer
                else {
                    randomLocation.getBlock().setType(Material.SNOW);
                }

            }
        }, 50L, 10L);

        // -------------------------------------------------------------------------------------------------------------
        // Damage players
        // -------------------------------------------------------------------------------------------------------------
        int damageTask = Bukkit.getScheduler().scheduleSyncRepeatingTask(main, () -> {
            if (!map.getPlayersInArena().isEmpty()) {
                for (Player p : map.getPlayersInArena()) {

                    assert p != null;

                    if (p.getGameMode() == GameMode.ADVENTURE) {

                        Location l = p.getLocation();
                        Block b = l.getBlock();
                        int finalDamage = 0;

                        // Half a heart if player is away from lights
                        if (b.getLightFromBlocks() < 8)
                            finalDamage += 1;

                        // Half a heart if player is above ice level
                        if (b.getY() >= currentY.get()) {
                            finalDamage += 2;
                        }

                        if (finalDamage > 0)
                            p.setFreezeTicks(100);

                        p.damage(finalDamage);

                    }

                }
            }
        }, startDelay, 20L);

        registerTasks(freezeTask, snowTask, damageTask);
    }

    private boolean checkIfBlockCanBecomeSnowLayer(Block b) {
        var blockBelow = b.getRelative(BlockFace.DOWN);

        // blocks without collision
        if (blockBelow.isPassable()) return false;

        // liquids
        if (blockBelow.isLiquid()) return false;

        // collision with more than 1 bounding box
        var voxelShape = blockBelow.getCollisionShape();
        if (voxelShape.getBoundingBoxes().size() != 1) return false;

        // non-cube bounding box
        BoundingBox boundingBox = (BoundingBox) voxelShape.getBoundingBoxes().toArray()[0];
        return boundingBox.getWidthX() == 1.0 &&
                boundingBox.getHeight() == 1.0 &&
                boundingBox.getWidthZ() == 1.0;
    }
}
