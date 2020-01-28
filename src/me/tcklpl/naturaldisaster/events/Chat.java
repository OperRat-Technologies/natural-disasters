package me.tcklpl.naturaldisaster.events;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class Chat implements Listener {

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        e.setFormat("%s" + ChatColor.DARK_GRAY + " : " + ChatColor.GRAY + "%s");
    }
}
