package me.tcklpl.naturaldisaster.disasters;

import me.tcklpl.naturaldisaster.util.BiomeUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LightningStrike;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class Thunderstorm extends Disaster {

    public Thunderstorm() {
        super("Thunderstorm", false, Material.CREEPER_HEAD, BiomeUtils.PrecipitationRequirements.SHOULD_RAIN);
    }

    @Override
    public void startDisaster() {
        //map.getWorld().setMonsterSpawnLimit(100);
        super.startDisaster();

        map.makeRain(false);

        Random r = random;

        HashMap<Location, Integer> creeperChargedSpawns = new HashMap<>();
        AtomicInteger timesRunned = new AtomicInteger(0);

        int taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(main, () -> {

            if ((timesRunned.incrementAndGet() % 3) == 0)
                for (Player p : map.getPlayersInArena()) {

                    assert p != null;
                    Location l = p.getLocation();

                    if (p.getGameMode() == GameMode.ADVENTURE) {

                        int chanceOfStrike = 15;
                        int playersNearby = 0;

                        // Lightning chance of spawning per factor
                        if (l.getY() >= Math.floorDiv(map.getMapSize().getY(), 2))
                            chanceOfStrike += 30;
                        if (map.getWorld().getHighestBlockYAt(l.getBlockX(), l.getBlockZ()) <= l.getBlockY())
                            chanceOfStrike += 50;
                        for (Entity nearby : p.getNearbyEntities(5, 2, 5)) {
                            if (nearby instanceof Player) {
                                playersNearby += 1;
                                chanceOfStrike += 20;
                            }
                        }

                        // Spawn lightning based of player chance of it
                        if (r.nextInt(100) < chanceOfStrike) {
                            LightningStrike ls = map.getWorld().spawn(map.getWorld().getHighestBlockAt(l.getBlockX(), l.getBlockZ()).getLocation(), LightningStrike.class);
                            Block b = ls.getLocation().getBlock();
                            for (int x = b.getX() - 1; x <= b.getX() + 1; x++) {
                                for (int y = b.getY() - 1; y <= b.getY() + 1; y++) {
                                    for (int z = b.getZ() - 1; z <= b.getZ() + 1; z++) {
                                        Block toBreak = map.getWorld().getBlockAt(x, y, z);
                                        toBreak.breakNaturally(new ItemStack(Material.AIR));
                                    }
                                }
                            }
                        }

                        // Add creeper charged spawn if there are more players nearby
                        if (playersNearby > 0)
                            if (!creeperChargedSpawns.containsKey(l))
                                creeperChargedSpawns.put(l, playersNearby);

                    }
                }

            if (creeperChargedSpawns.size() > 0) {
                for (Map.Entry<Location, Integer> entry : creeperChargedSpawns.entrySet()) {
                    if (r.nextInt(4) <= entry.getValue()) {
                        Location l = entry.getKey();
                        boolean spawned = false;
                        while (!spawned) {
                            Location temp = l.add(r.nextInt(7) - 3, 0, r.nextInt(7) - 3);
                            if (temp.getBlock().getRelative(BlockFace.UP).getType() == Material.AIR) {
                                Creeper c = map.getWorld().spawn(temp, Creeper.class);
                                c.setPowered(true);
                                spawned = true;
                            }
                        }
                    }
                }
                creeperChargedSpawns.clear();
            }


        }, startDelay, 20L);

        registerTasks(taskId);

    }
}
