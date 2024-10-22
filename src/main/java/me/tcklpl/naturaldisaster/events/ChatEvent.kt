package me.tcklpl.naturaldisaster.events

import org.bukkit.ChatColor
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerChatEvent

object ChatEvent : Listener {

    @EventHandler
    fun onChat(e: AsyncPlayerChatEvent) {
        e.format = "%s${ChatColor.DARK_GRAY}: ${ChatColor.GRAY}%s"
    }
}
