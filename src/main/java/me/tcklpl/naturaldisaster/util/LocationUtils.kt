package me.tcklpl.naturaldisaster.util

import org.bukkit.Location
import kotlin.math.max
import kotlin.math.min

object LocationUtils {

    fun isBetween(target: Location, a: Location, b: Location): Boolean {
        val minX = min(a.x, b.x)
        val minY = min(a.y, b.y)
        val minZ = min(a.z, b.z)

        val maxX = max(a.x, b.x)
        val maxY = max(a.y, b.y)
        val maxZ = max(a.z, b.z)

        return target.x >= minX && target.x <= maxX && target.y >= minY && target.y <= maxY && target.z >= minZ && target.z <= maxZ
    }
}
