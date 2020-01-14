package me.tcklpl.naturaldisaster.events;

import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;

public class Explosion implements Listener {

    @EventHandler
    public void onExplode(EntityExplodeEvent e) {
        if (e.getEntity() instanceof TNTPrimed) {
            e.setCancelled(true);
        }
    }
}
