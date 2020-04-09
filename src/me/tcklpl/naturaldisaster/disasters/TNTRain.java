package me.tcklpl.naturaldisaster.disasters;

import me.tcklpl.naturaldisaster.map.DisasterMap;
import me.tcklpl.naturaldisaster.reflection.ReflectionUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class TNTRain extends Disaster {

    private final JavaPlugin main;
    private Random r;

    public TNTRain(DisasterMap map, JavaPlugin main) {
        super(map, main);
        this.main = main;
        name = "TNT Rain";
        hint = "Procure abrigo.";
        playable = true;
        icon = Material.TNT;
    }

    @Override
    public void startDisaster() {

        super.startDisaster();

        r = random;

        map.setArenaRandomBiomeBasedOnPrecipitationType(ReflectionUtils.PrecipitationType.ALL);
        map.makeRain(true);

        AtomicInteger tntToSpawn = new AtomicInteger(1);
        AtomicInteger timesRunned = new AtomicInteger(0);

        taskId = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(main, () -> {

            for (int i = 0; i < tntToSpawn.get(); i++) {
                int x = map.minX + r.nextInt(map.gapX);
                int z = map.minZ + r.nextInt(map.gapZ);
                Location loc = new Location(map.getWorld(), x, map.top, z);

                TNTPrimed tnt = Objects.requireNonNull(map.getWorld()).spawn(loc, TNTPrimed.class);

                tnt.setTicksLived(5);

                tnt.setFuseTicks(20 + 20 * Math.floorDiv(map.top - map.floor, 20)); // 1s inicial + 1s p/ cada 20 blocos
            }

            if ((timesRunned.incrementAndGet() % 10) == 0)
                tntToSpawn.addAndGet(1);

        }, startDelay, 20L);

    }

}
