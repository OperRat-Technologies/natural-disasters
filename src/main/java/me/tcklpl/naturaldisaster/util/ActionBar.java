package me.tcklpl.naturaldisaster.util;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class ActionBar {

    private final TextComponent textComponent;


    public ActionBar(String text) {
        textComponent = new TextComponent(text);
    }

    public void sendToPlayer(Player p) {
        p.spigot().sendMessage(ChatMessageType.ACTION_BAR, textComponent);
    }

    public void sendToAll() {
        for (Player p : Bukkit.getOnlinePlayers())
            sendToPlayer(p);
    }
}
