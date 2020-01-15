package me.tcklpl.naturaldisaster.events;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;

public class PickItem implements Listener {

    @EventHandler
    public void onPick(EntityPickupItemEvent e) {
        if (e.getEntity() instanceof Player) {
            Player p = (Player) e.getEntity();
            if (p.getGameMode().equals(GameMode.ADVENTURE))
                e.setCancelled(true);
        }
    }
}
