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

        int x1 = map.getPos1().getBlockX();
        int x2 = map.getPos2().getBlockX();
        int y1 = map.getPos1().getBlockY();
        int y2 = map.getPos2().getBlockY();
        int z1 = map.getPos1().getBlockZ();
        int z2 = map.getPos2().getBlockZ();

        int baseX, baseZ, top, gapX, gapZ;

        baseX = Math.min(x1, x2);
        baseZ = Math.min(z1, z2);
        top = Math.max(y1, y2);
        gapX = Math.max(x1, x2) - baseX;
        gapZ = Math.max(z1, z2) - baseZ;

        taskId = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(main, () -> {
            int x = baseX + r.nextInt(gapX);
            int z = baseZ + r.nextInt(gapZ);
            Location loc = new Location(map.getWorld(), x, top, z);

            TNTPrimed tnt = Objects.requireNonNull(map.getWorld()).spawn(loc, TNTPrimed.class);

            tnt.setTicksLived(5);
            tnt.setFuseTicks(40);

        }, 0L, 15L);

    }

}
