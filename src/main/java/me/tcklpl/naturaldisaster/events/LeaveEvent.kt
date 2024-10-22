package me.tcklpl.naturaldisaster.events

import me.tcklpl.naturaldisaster.GameStatus
import me.tcklpl.naturaldisaster.NaturalDisaster
import org.bukkit.ChatColor
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent

object LeaveEvent : Listener {

    @EventHandler
    fun onLeave(e: PlayerQuitEvent) {
        if (NaturalDisaster.getGameManager().currentStatus != GameStatus.IN_LOBBY) NaturalDisaster.getGameManager()
            .registerPlayerDeath(e.player)
        e.quitMessage = "${ChatColor.GRAY}${e.player.name} saiu do servidor"
    }
}
