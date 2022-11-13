package me.tcklpl.naturaldisaster.events;

import org.bukkit.entity.Bat;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;

public class MobSpawn implements Listener {

    @EventHandler
    public void onMobSpawn(EntitySpawnEvent e) {
        if (e.getEntity() instanceof Bat)
            e.setCancelled(true);
    }
}
