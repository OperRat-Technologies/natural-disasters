package me.tcklpl.naturaldisaster.disasters.tornado

import org.bukkit.Location
import org.bukkit.entity.Entity

class Tornado(private val position: Location) {
    private val windField = TornadoWindField(position)

    fun applyVelocityForEntities(entities: List<Entity>) {
        for (entity in entities) {
            val velocity = windField.calculateObjectVelocityFromPosition(entity.location)
            entity.velocity = velocity.toBukkitVector()
        }
    }
}