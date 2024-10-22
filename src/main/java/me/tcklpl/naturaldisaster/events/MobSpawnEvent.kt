package me.tcklpl.naturaldisaster.events

import org.bukkit.entity.Bat
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntitySpawnEvent

object MobSpawnEvent : Listener {

    @EventHandler
    fun onMobSpawn(e: EntitySpawnEvent) {
        if (e.getEntity() is Bat) e.isCancelled = true
    }
}
