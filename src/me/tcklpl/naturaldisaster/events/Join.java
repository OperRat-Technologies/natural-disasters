package me.tcklpl.naturaldisaster.events;

import me.tcklpl.naturaldisaster.util.ActionBar;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class Join implements Listener {

    @EventHandler
    public void onLogin(PlayerJoinEvent e) {
        ActionBar a = new ActionBar(ChatColor.GOLD + "Texxxxxxti");
        a.sendToPlayer(e.getPlayer());
    }
}
