package me.tcklpl.naturaldisaster.disasters;

import me.tcklpl.naturaldisaster.map.DisasterMap;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.LightningStrike;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Random;

public class Thunderstorm extends Disaster {

    public Thunderstorm(DisasterMap map, JavaPlugin main) {
        super(map, main);
        name = "Thunderstorm";
    }

    @Override
    public void startDisaster() {
        super.startDisaster();

        map.makeRain(false);

        Random r = random;

        int strikechance = 10;

        taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(main, () -> {

            for (String playerName : map.getPlayersInArena()) {

                Player p = Bukkit.getPlayer(playerName);
                assert p != null;

                if (p.getGameMode() == GameMode.ADVENTURE) {

                    if (r.nextInt(100) <= strikechance) {

                        Location l = p.getLocation();

                        int offsetX = r.nextInt(5) - 3;
                        int offsetZ = r.nextInt(5) - 3;

                        l.setX(l.getBlockX() + offsetX);
                        l.setZ(l.getBlockZ() + offsetZ);


                        int topo = Math.max(map.getPos1().getBlockY(), map.getPos2().getBlockY());
                        boolean blockAbove = false;
                        int y;
                        for (y = l.getBlockY() + 1; y <= topo && !blockAbove; y++) {
                            if (map.getWorld().getBlockAt(l.getBlockX(), y, l.getBlockZ()).getBlockData().getMaterial() != Material.AIR)
                                blockAbove = true;
                        }
                        if (blockAbove) {
                            Location blockLoc = new Location(map.getWorld(), l.getBlockX(), y, l.getBlockZ());
                            map.getWorld().spawn(blockLoc, LightningStrike.class);

                            map.getWorld().getBlockAt(blockLoc).setType(Material.FIRE);
                            map.getWorld().getBlockAt(blockLoc.add(1, 0, 0)).setType(Material.FIRE);
                            map.getWorld().getBlockAt(blockLoc.add(0, 0, 1)).setType(Material.FIRE);
                            map.getWorld().getBlockAt(blockLoc.subtract(1, 0, 0)).setType(Material.FIRE);
                            map.getWorld().getBlockAt(blockLoc.subtract(0, 0, 1)).setType(Material.FIRE);

                        } else {
                            map.getWorld().spawn(l, LightningStrike.class);
                        }


                    }

                }


            }

        }, startDelay, 20L);

    }
}
