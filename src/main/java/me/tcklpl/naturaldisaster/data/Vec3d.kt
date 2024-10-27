package me.tcklpl.naturaldisaster.data

import org.bukkit.util.Vector

data class Vec3d(var x: Double, var y: Double, var z: Double) {

    fun toBukkitVector() = Vector(x, y, z)
}
