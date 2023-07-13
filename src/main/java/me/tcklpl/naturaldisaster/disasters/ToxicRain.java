package me.tcklpl.naturaldisaster.disasters;

import me.tcklpl.naturaldisaster.util.BiomeUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class ToxicRain extends Disaster {

    public ToxicRain() {
        super("Toxic Rain", true, Material.LIME_DYE, BiomeUtils.PrecipitationRequirements.SHOULD_RAIN);
    }

    @Override
    public void startDisaster() {

        super.startDisaster();

        map.makeRain(false);

        Random r = random;

        AtomicInteger blocksToBreak = new AtomicInteger(5);
        AtomicInteger timesExecuted = new AtomicInteger(0);
        AtomicInteger currentDamage = new AtomicInteger(1);
        AtomicInteger poisonDuration = new AtomicInteger(2);
        AtomicInteger poisonStrenght = new AtomicInteger(1);

        int taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(main, () -> {

            timesExecuted.getAndIncrement();

            if (map.getPlayersInArena().size() > 0)
                for (Player p : map.getPlayersInArena()) {

                    // Damage player
                    assert p != null;
                    if (p.getGameMode() == GameMode.ADVENTURE) {
                        Location l = p.getLocation();
                        int topo = Math.max(map.getPos1().getBlockY(), map.getPos2().getBlockY());
                        boolean blockAbove = false;
                        for (int y = l.getBlockY() + 1; y <= topo && !blockAbove; y++) {
                            if (map.getWorld().getBlockAt(l.getBlockX(), y, l.getBlockZ()).getBlockData().getMaterial() != Material.AIR)
                                blockAbove = true;
                        }
                        if (!blockAbove) {
                            p.damage(currentDamage.get());
                            p.addPotionEffect(PotionEffectType.POISON.createEffect(poisonDuration.get(), poisonStrenght.get()));
                        }
                    }

                    // Destroy blocks
                    for (int i = 0; i < blocksToBreak.get(); i++) {
                        int blockX = map.getLowestCoordsLocation().getBlockX() + r.nextInt(map.getMapSize().getX());
                        int blockY = map.getHighestCoordsLocation().getBlockY();
                        int blockZ = map.getLowestCoordsLocation().getBlockZ() + r.nextInt(map.getMapSize().getZ());
                        for (; blockY >= map.getLowestCoordsLocation().getBlockY(); blockY--) {
                            Block b = map.getWorld().getBlockAt(blockX, blockY, blockZ);
                            Block bUnder = map.getWorld().getBlockAt(blockX, blockY - 1, blockZ);
                            if (b.getBlockData().getMaterial() != Material.AIR) {
                                if (bUnder.getType() == Material.AIR || b.getBlockData().getMaterial().isSolid())
                                    b.setType(Material.LIME_WOOL);
                                else b.setType(Material.LIME_CARPET);
                                Bukkit.getScheduler().scheduleSyncDelayedTask(main, () -> b.setType(Material.AIR), 40L);
                                break;
                            }
                        }
                    }

                    // Increase difficulty with time
                    if ((timesExecuted.get() % 10) == 0) {
                        blocksToBreak.addAndGet(5);
                    }

                    if ((timesExecuted.get() % 20) == 0)
                        poisonDuration.addAndGet(20);

                    if ((timesExecuted.get() % 30) == 0)
                        poisonStrenght.addAndGet(1);

                    if ((timesExecuted.get() % 60) == 0)
                        currentDamage.addAndGet(1);
                }

        }, startDelay, 20L);

        registerTasks(taskId);
    }
}