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
        if (NaturalDisaster.instance.gameManager.currentStatus != GameStatus.IN_LOBBY) NaturalDisaster.instance.gameManager
            .registerPlayerDeath(e.player)
        e.quitMessage = "${ChatColor.GRAY}${e.player.name} saiu do servidor"
    }
}
