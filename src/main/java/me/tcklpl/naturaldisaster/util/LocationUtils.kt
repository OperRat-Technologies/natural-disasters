package me.tcklpl.naturaldisaster.util;

import org.bukkit.Location;

public class LocationUtils {

    public static boolean isBetween(Location target, Location a, Location b) {
        var minX = Math.min(a.getX(), b.getX());
        var minY = Math.min(a.getY(), b.getY());
        var minZ = Math.min(a.getZ(), b.getZ());

        var maxX = Math.max(a.getX(), b.getX());
        var maxY = Math.max(a.getY(), b.getY());
        var maxZ = Math.max(a.getZ(), b.getZ());

        return target.getX() >= minX && target.getX() <= maxX &&
                target.getY() >= minY && target.getY() <= maxY &&
                target.getZ() >= minZ && target.getZ() <= maxZ;
    }

}
