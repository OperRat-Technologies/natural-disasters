package me.tcklpl.naturaldisaster.events

import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockFadeEvent

object IceMeltEvent : Listener {

    @EventHandler
    fun onIceMelt(e: BlockFadeEvent) {
        if (e.getBlock().type == Material.ICE) e.isCancelled = true
    }
}
