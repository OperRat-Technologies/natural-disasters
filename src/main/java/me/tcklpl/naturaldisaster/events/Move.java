package me.tcklpl.naturaldisaster.events;

import me.tcklpl.naturaldisaster.GameStatus;
import me.tcklpl.naturaldisaster.NaturalDisaster;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.Objects;

public class Move implements Listener {

    @EventHandler
    public void onMove(PlayerMoveEvent e) {

        if (NaturalDisaster.getGameManager().getCurrentStatus() == GameStatus.STARTING) {
            Location l = e.getFrom();
            Objects.requireNonNull(e.getTo()).setX(l.getX());
            Objects.requireNonNull(e.getTo()).setZ(l.getZ());
        }
    }
}
