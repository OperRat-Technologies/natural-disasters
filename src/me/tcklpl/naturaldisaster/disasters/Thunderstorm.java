package me.tcklpl.naturaldisaster.disasters;

import me.tcklpl.naturaldisaster.map.DisasterMap;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LightningStrike;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Thunderstorm extends Disaster {

    public Thunderstorm(DisasterMap map, JavaPlugin main) {
        super(map, main);
        name = "Thunderstorm";
        hint = "Evite locais altos.";
    }

    @Override
    public void startDisaster() {
        super.startDisaster();

        map.makeRain(false);

        Random r = random;

        int strikechance = 10;

        taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(main, () -> {

            HashMap<Location, Integer> creeperChargedSpawns = new HashMap<>();

            for (String playerName : map.getPlayersInArena()) {

                Player p = Bukkit.getPlayer(playerName);
                assert p != null;
                Location l = p.getLocation();

                if (p.getGameMode() == GameMode.ADVENTURE) {

                    int chanceOfStrike = 0;
                    int playersNearby = 0;

                    // Lightning chance of spawning per factor
                    if (l.getY() >= Math.floorDiv(map.top - map.floor, 2))
                        chanceOfStrike += 30;
                    if (map.getWorld().getHighestBlockYAt(l.getBlockX(), l.getBlockZ()) <= l.getBlockY())
                        chanceOfStrike += 80;
                    for (Entity nearby : p.getNearbyEntities(5, 2, 5)) {
                        if (nearby instanceof Player) {
                            playersNearby += 1;
                            chanceOfStrike += 10;
                        }
                    }

                    // Spawn lightning based of player chance of it
                    if (chanceOfStrike != 0)
                    if (r.nextInt(100) < chanceOfStrike) {
                        map.getWorld().spawn(map.getWorld().getHighestBlockAt(l.getBlockX(), l.getBlockZ()).getLocation(), LightningStrike.class);
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

    }
}
