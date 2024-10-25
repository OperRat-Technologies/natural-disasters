package me.tcklpl.naturaldisaster.shop

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent

object ShopInventoryClick : Listener {

    @EventHandler
    fun onShopInteract(e: InventoryClickEvent) {
        if (e.view.title.equals("Shop", ignoreCase = true)) {
            e.isCancelled = true
        }
    }
}
