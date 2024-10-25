package me.tcklpl.naturaldisaster.events

import me.tcklpl.naturaldisaster.GameStatus
import me.tcklpl.naturaldisaster.NaturalDisaster
import org.bukkit.ChatColor
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.server.ServerListPingEvent

object MotdEvent : Listener {

    @EventHandler
    fun onServerListPing(e: ServerListPingEvent) {
        when (NaturalDisaster.instance.gameManager.currentStatus) {
            GameStatus.IN_LOBBY -> e.motd = "${ChatColor.GREEN}Em lobby"
            GameStatus.STARTING -> e.motd = "${ChatColor.YELLOW}Iniciando..."
            GameStatus.IN_GAME -> e.motd = "${ChatColor.RED}Em jogo"
            else -> e.motd = "${ChatColor.RED}???"
        }
    }
}