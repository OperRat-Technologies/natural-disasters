package me.tcklpl.naturaldisaster.events;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerListPingEvent;

public class Motd implements Listener {

    @EventHandler
    public void onServerListPink(ServerListPingEvent e) {
        e.setMotd(ChatColor.YELLOW + "Preparing...");
    }
}
