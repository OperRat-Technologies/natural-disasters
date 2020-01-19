package me.tcklpl.naturaldisaster.events;

import me.tcklpl.naturaldisaster.map.MapManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class Leave implements Listener {

    @EventHandler
    public void onLeave(PlayerQuitEvent e) {
        MapManager.getInstance().updateArenaForDeadPlayer(e.getPlayer().getName());
        e.setQuitMessage("");
    }

}
