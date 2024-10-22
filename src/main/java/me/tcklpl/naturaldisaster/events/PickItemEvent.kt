package me.tcklpl.naturaldisaster.events

import org.bukkit.GameMode
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityPickupItemEvent

object PickItemEvent : Listener {

    @EventHandler
    fun onPick(e: EntityPickupItemEvent) {
        var entity = e.entity
        if (entity !is Player) return
        if (entity.gameMode == GameMode.ADVENTURE) e.isCancelled = true
    }
}
