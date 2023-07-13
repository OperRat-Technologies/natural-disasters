package me.tcklpl.naturaldisaster.disasters;

import me.tcklpl.naturaldisaster.util.BiomeUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.TNTPrimed;

import java.util.Objects;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class TNTRain extends Disaster {

    private Random r;

    public TNTRain() {
        super("TNT Rain", true, Material.TNT, BiomeUtils.PrecipitationRequirements.ANYTHING);
    }

    @Override
    public void startDisaster() {

        super.startDisaster();

        r = random;

        map.makeRain(true);

        AtomicInteger tntToSpawn = new AtomicInteger(1);
        AtomicInteger timesRunned = new AtomicInteger(0);

        int taskId = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(main, () -> {

            for (int i = 0; i < tntToSpawn.get(); i++) {
                int x = map.getLowestCoordsLocation().getBlockX() + r.nextInt(map.getMapSize().getX());
                int z = map.getLowestCoordsLocation().getBlockZ() + r.nextInt(map.getMapSize().getZ());
                Location loc = new Location(map.getWorld(), x, map.getHighestCoordsLocation().getBlockY(), z);

                TNTPrimed tnt = Objects.requireNonNull(map.getWorld()).spawn(loc, TNTPrimed.class);

                tnt.setTicksLived(5);

                tnt.setFuseTicks(20 + map.getMapSize().getY()); // 1s inicial + 1s p/ cada 20 blocos
            }

            if ((timesRunned.incrementAndGet() % 10) == 0)
                tntToSpawn.addAndGet(1);

        }, startDelay, 20L);

        registerTasks(taskId);

    }

}
