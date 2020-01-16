package me.tcklpl.naturaldisaster.disasters;

import me.tcklpl.naturaldisaster.map.DisasterMap;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;
import java.util.Random;

public class TNTRain extends Disaster {

    private JavaPlugin main;
    private Random r;

    public TNTRain(DisasterMap map, JavaPlugin main) {
        super(map, main);
        this.main = main;
        r = new Random();
        name = "TNT Rain";
    }

    @Override
    public void startDisaster() {

        super.startDisaster();

        updateBlockGap();

        taskId = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(main, () -> {
            int x = minX + r.nextInt(gapX);
            int z = minZ + r.nextInt(gapZ);
            Location loc = new Location(map.getWorld(), x, top, z);

            TNTPrimed tnt = Objects.requireNonNull(map.getWorld()).spawn(loc, TNTPrimed.class);


            tnt.setTicksLived(5);

            tnt.setFuseTicks(20 + 20 * Math.floorDiv(top - floor, 20)); // 1s inicial + 1s p/ cada 20 blocos

        }, 0L, 15L);

    }

}
