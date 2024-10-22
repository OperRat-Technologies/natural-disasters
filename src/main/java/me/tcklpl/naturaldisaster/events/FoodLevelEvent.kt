package me.tcklpl.naturaldisaster.events

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.FoodLevelChangeEvent

object FoodLevelEvent : Listener {

    @EventHandler
    fun onFoodLevelDecrease(e: FoodLevelChangeEvent) {
        e.foodLevel = 20
        e.isCancelled = true
    }
}
