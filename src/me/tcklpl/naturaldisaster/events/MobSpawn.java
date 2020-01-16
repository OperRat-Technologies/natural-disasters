package me.tcklpl.naturaldisaster.events;

import me.tcklpl.naturaldisaster.map.MapManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;

public class MobSpawn implements Listener {

    @EventHandler
    public void onMobSpawn(CreatureSpawnEvent e) {
        if (MapManager.getInstance().isIsInGame())
            e.setCancelled(true);
    }
}
