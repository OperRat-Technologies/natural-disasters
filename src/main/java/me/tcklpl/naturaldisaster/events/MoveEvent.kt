package me.tcklpl.naturaldisaster.events

import me.tcklpl.naturaldisaster.GameStatus
import me.tcklpl.naturaldisaster.NaturalDisaster
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent

object MoveEvent : Listener {

    @EventHandler
    fun onMove(e: PlayerMoveEvent) {
        if (NaturalDisaster.getGameManager().currentStatus == GameStatus.STARTING) {
            val l = e.from;
            e.to?.x = l.x;
            e.to?.z = l.z;
        }
    }
}