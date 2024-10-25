package me.tcklpl.naturaldisaster.map

import org.bukkit.Location
import org.bukkit.Material
import java.util.ArrayList
import java.util.Objects

class TempDisasterMap(var name: String, var worldName: String) {
    var pos1: Location? = null
    var pos2: Location? = null
    val spawns = ArrayList<Location>()
    var icon: Material? = null

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false
        val that = o as TempDisasterMap
        return name == that.name && worldName == that.worldName
    }

    override fun hashCode(): Int {
        return Objects.hash(name, worldName)
    }

    fun addSpawn(spawn: Location) {
        this.spawns.add(spawn)
    }

    fun isComplete(): Boolean {
        return pos1 != null && pos2 != null && spawns.size >= 24 && icon != null
    }
}
