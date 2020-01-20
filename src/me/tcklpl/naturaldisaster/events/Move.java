package me.tcklpl.naturaldisaster.events;

import me.tcklpl.naturaldisaster.GameStatus;
import me.tcklpl.naturaldisaster.map.MapManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class Move implements Listener {

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        if (MapManager.getInstance().getCurrentStatus() == GameStatus.STARTING)
            e.setCancelled(true);
    }
}
