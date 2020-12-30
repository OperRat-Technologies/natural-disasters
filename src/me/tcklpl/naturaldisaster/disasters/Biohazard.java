package me.tcklpl.naturaldisaster.disasters;

import me.tcklpl.naturaldisaster.map.ArenaBiomeType;
import me.tcklpl.naturaldisaster.map.DisasterMap;
import me.tcklpl.naturaldisaster.reflection.ReflectionUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffectType;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class Biohazard extends Disaster {

    private int nauseaMinX, nauseaMaxX, nauseaMinY, nauseaMaxY, nauseaMinZ, nauseaMaxZ,
            venomMinX, venomMaxX, venomMinY, venomMaxY, venomMinZ, venomMaxZ,
            decompMinX, decompMaxX, decompMinY, decompMaxY, decompMinZ, decompMaxZ,
            currentNauseaOffset = 2, currentVenomOffset = 0, currentDecompOffset = 0;
    private int sourceX, sourceY, sourceZ;

    public Biohazard() {
        super("Biohazard", false, Material.CHORUS_FRUIT, ReflectionUtils.PrecipitationType.ALL, ArenaBiomeType.RANDOM_PER_PRECIPITATION);
    }

    private void updateEffectCoordinates() {
        nauseaMinX = sourceX - currentNauseaOffset;
        nauseaMaxX = sourceX + currentNauseaOffset;
        nauseaMinY = sourceY - currentNauseaOffset;
        nauseaMaxY = sourceY + currentNauseaOffset;
        nauseaMinZ = sourceZ - currentNauseaOffset;
        nauseaMaxZ = sourceZ + currentNauseaOffset;

        venomMinX = sourceX - currentVenomOffset;
        venomMaxX = sourceX + currentVenomOffset;
        venomMinY = sourceY - currentVenomOffset;
        venomMaxY = sourceY + currentVenomOffset;
        venomMinZ = sourceZ - currentVenomOffset;
        venomMaxZ = sourceZ + currentVenomOffset;

        decompMinX = sourceX - currentDecompOffset;
        decompMaxX = sourceX + currentDecompOffset;
        decompMinY = sourceY - currentDecompOffset;
        decompMaxY = sourceY + currentDecompOffset;
        decompMinZ = sourceZ - currentDecompOffset;
        decompMaxZ = sourceZ + currentDecompOffset;
    }

    private boolean theresBlockInY(int x, int z) {
        for (int y = map.floor + 1; y <= map.top; y++) {
            if (map.getWorld().getBlockAt(x, y, z).getType() != Material.AIR)
                return true;
        }
        return false;
    }


    @Override
    public void startDisaster() {
        super.startDisaster();

        Random r = new Random();

        do {
            sourceX = map.minX + r.nextInt(map.gapX);
            sourceZ = map.minZ + r.nextInt(map.gapZ);
        } while (!theresBlockInY(sourceX, sourceZ));

        sourceY = map.getWorld().getHighestBlockYAt(sourceX, sourceZ);

        FallingBlock sourceFallingBlock = map.getWorld().spawnFallingBlock(new Location(map.getWorld(), sourceX, sourceY + 20, sourceZ), Bukkit.createBlockData(Material.SPONGE));
        sourceFallingBlock.setDropItem(false);
        sourceFallingBlock.setHurtEntities(true);

        AtomicInteger timesRunned = new AtomicInteger(0);

        int finalSourceX = sourceX;
        int finalSourceZ = sourceZ;

        // First update otherwise it will compare to null
        updateEffectCoordinates();

        Bukkit.getScheduler().scheduleSyncDelayedTask(main, () -> {
            map.getWorld().createExplosion(new Location(map.getWorld(), sourceX, sourceY, sourceZ), 3);
        }, 30L);

        int taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(main, () -> {


            if ((timesRunned.incrementAndGet() % 2) == 0) {
                currentNauseaOffset++;
                updateEffectCoordinates();
            }
            if ((timesRunned.get() % 4) == 0) {
                currentVenomOffset++;
                updateEffectCoordinates();
            }
            if ((timesRunned.get() % 8) == 0) {
                currentDecompOffset++;
                updateEffectCoordinates();
            }

            for (Player p : map.getPlayersInArena()) {
                assert p != null;
                Location l = p.getLocation();
                int pX = l.getBlockX();
                int pY = l.getBlockY();
                int pZ = l.getBlockZ();
                if (pX >= nauseaMinX && pX <= nauseaMaxX && pY >= nauseaMinY && pY <= nauseaMaxY && pZ >= nauseaMinZ && pZ <= nauseaMaxZ) {
                    if (!p.hasPotionEffect(PotionEffectType.CONFUSION))
                        p.addPotionEffect(PotionEffectType.CONFUSION.createEffect(120, 3));
                    if (pX >= venomMinX && pX <= venomMaxX && pY >= venomMinY && pY <= venomMaxY && pZ >= venomMinZ && pZ <= venomMaxZ) {
                        p.addPotionEffect(PotionEffectType.POISON.createEffect(40, 1));
                        if (pX >= decompMinX && pX <= decompMaxX && pY >= decompMinY && pY <= decompMaxY && pZ >= decompMinZ && pZ <= decompMaxZ) {
                            p.addPotionEffect(PotionEffectType.WITHER.createEffect(40, 1));
                        }
                    }
                }
            }

        }, startDelay, 20L);

        registerTasks(taskId);

    }
}
