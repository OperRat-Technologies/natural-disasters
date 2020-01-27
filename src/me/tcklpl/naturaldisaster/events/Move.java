package me.tcklpl.naturaldisaster.events;

import me.tcklpl.naturaldisaster.GameStatus;
import me.tcklpl.naturaldisaster.map.MapManager;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.Objects;

public class Move implements Listener {

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        if (MapManager.getInstance().getCurrentStatus() == GameStatus.STARTING) {
            Location l = e.getFrom();
            Objects.requireNonNull(e.getTo()).setX(e.getFrom().getX());
            Objects.requireNonNull(e.getTo()).setZ(e.getFrom().getZ());
        }
    }
}
