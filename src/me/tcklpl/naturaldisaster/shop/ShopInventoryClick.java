package me.tcklpl.naturaldisaster.shop;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class ShopInventoryClick implements Listener {

    @EventHandler
    public void onShopInteract(InventoryClickEvent e) {
        if (e.getView().getTitle().equalsIgnoreCase("Shop")) {
            e.setCancelled(true);

        }
    }

}
