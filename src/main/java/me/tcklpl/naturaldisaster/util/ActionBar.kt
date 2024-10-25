package me.tcklpl.naturaldisaster.util

import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.Bukkit
import org.bukkit.entity.Player

class ActionBar(text: String) {
    private val textComponent: TextComponent = TextComponent(text)

    fun sendToPlayer(p: Player) {
        p.spigot().sendMessage(ChatMessageType.ACTION_BAR, textComponent)
    }

    fun sendToAll() {
        for (p in Bukkit.getOnlinePlayers()) sendToPlayer(p)
    }
}
