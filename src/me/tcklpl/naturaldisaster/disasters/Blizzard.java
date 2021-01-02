package me.tcklpl.naturaldisaster.disasters;

import me.tcklpl.naturaldisaster.reflection.ReflectionWorldUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Blizzard extends Disaster {

    public Blizzard() {
        super("Blizzard", true, Material.SNOWBALL, ReflectionWorldUtils.Precipitation.SNOW);
    }

    @Override
    public void startDisaster() {

        super.startDisaster();

        Random r = random;

        HashMap<Integer, List<Block>> blocksToChangePerLevel = new HashMap<>();
        List<Chunk> changedChunks = new ArrayList<>();

        for (int y = map.top; y >= map.floor; y--) {
            List<Block> layerBlocks = new ArrayList<>();

            for (int x = map.minX; x <= map.minX + map.gapX; x++) {
                for (int z = map.minZ; z <= map.minZ + map.gapZ; z++) {
                    Block b = map.getWorld().getBlockAt(x, y, z);
                    if (b.getType() != Material.AIR && !b.isPassable() && (!(b.getState() instanceof InventoryHolder)))
                        layerBlocks.add(b);
                }
            }
            blocksToChangePerLevel.put(y, layerBlocks);
        }

        map.makeRain(false);

        AtomicInteger timesRunned = new AtomicInteger(0);
        AtomicInteger nextIteration = new AtomicInteger(10 + r.nextInt(6));
        AtomicInteger currentY = new AtomicInteger(map.getMaxYLevel());

        List<Material> blockPallete = new ArrayList<>();
        blockPallete.add(Material.PACKED_ICE);
        blockPallete.add(Material.ICE);

        int taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(main, () -> {

            if (currentY.get() >= map.floor)
            if ((timesRunned.incrementAndGet() % nextIteration.get()) == 0) {

                nextIteration.set(10 + r.nextInt(6));
                map.bufferedReplaceBlocks(blocksToChangePerLevel.get(currentY.getAndDecrement()), blockPallete, 500, false);

            }

            if (map.getPlayersInArena().size() > 0)
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
                    p.damage(finalDamage);

                }

            }

        }, startDelay, 20L);

        registerTasks(taskId);

    }
}
