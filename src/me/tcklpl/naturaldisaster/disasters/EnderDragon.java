package me.tcklpl.naturaldisaster.disasters;

import me.tcklpl.naturaldisaster.map.ArenaBiomeType;
import me.tcklpl.naturaldisaster.map.DisasterMap;
import me.tcklpl.naturaldisaster.reflection.ReflectionUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.*;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.atomic.AtomicReference;

public class EnderDragon extends Disaster {

    /**
     * Constructor to abstract disaster class, to be used inside children.
     *
     * @param map  initially null, to be setted later.
     * @param main main reference needed to schedule tasks.
     */
    public EnderDragon(DisasterMap map, JavaPlugin main) {
        super(map, main);
        name = "Ender Dragon";
        hint = "Nenhuma";
        playable = false;
        icon = Material.DRAGON_EGG;
        precipitationType = ReflectionUtils.PrecipitationType.ALL;
        arenaBiomeType = ArenaBiomeType.RANDOM_PER_PRECIPITATION;
    }

    @Override
    public void startDisaster() {
        super.startDisaster();

        int epicenterX = map.minX + Math.floorDiv(map.gapX, 3) + random.nextInt(Math.floorDiv(map.gapX, 3));
        int epicenterZ = map.minZ + Math.floorDiv(map.gapZ, 3) + random.nextInt(Math.floorDiv(map.gapZ, 3));

        Location spawn = new Location(map.getWorld(), epicenterX, map.top, epicenterZ);
        AtomicReference<org.bukkit.entity.EnderDragon> enderDragon = new AtomicReference<>();

        map.getWorld().setTime(18000);

        Bukkit.getScheduler().scheduleSyncDelayedTask(main, () -> {
            enderDragon.set(map.getWorld().spawn(spawn, org.bukkit.entity.EnderDragon.class));
        }, startDelay);

        taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(main, () -> {

            for (Player p : map.getPlayersInArena()) {

                if (p.getGameMode() != GameMode.ADVENTURE)
                    continue;

                int nearbyPlayers = 0;
                for (Entity en : p.getNearbyEntities(3, 2, 3)) {
                    if (en instanceof Player)
                        nearbyPlayers++;
                }
                // Spawn endermans if there are 2 or more players nearby
                if (nearbyPlayers > 1) {
                    Location l = p.getLocation();
                    boolean spawned = false;
                    while (!spawned) {
                        Location temp = l.add(random.nextInt(7) - 3, 0, random.nextInt(7) - 3);
                        if (temp.getBlock().getRelative(BlockFace.UP).getType() == Material.AIR) {
                            if (random.nextBoolean()) {
                                Enderman en = map.getWorld().spawn(temp, Enderman.class);
                                en.setTarget(p);
                                en.setSilent(true);
                            } else {
                                Endermite em = map.getWorld().spawn(temp, Endermite.class);
                                em.setTarget(p);
                            }
                            spawned = true;
                        }
                    }
                }
            }

        }, startDelay, 20L);
    }
}
