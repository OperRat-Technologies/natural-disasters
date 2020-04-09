package me.tcklpl.naturaldisaster.events;

import me.tcklpl.naturaldisaster.GameStatus;
import me.tcklpl.naturaldisaster.NaturalDisaster;
import me.tcklpl.naturaldisaster.map.MapManager;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class Leave implements Listener {

    @EventHandler
    public void onLeave(PlayerQuitEvent e) {
        if (NaturalDisaster.getMapManager().getCurrentStatus() != GameStatus.IN_LOBBY)
            NaturalDisaster.getMapManager().updateArenaForDeadPlayer(e.getPlayer());
        e.setQuitMessage(ChatColor.GRAY + e.getPlayer().getName() + ChatColor.GRAY + " saiu do servidor");
        NaturalDisaster.getAuthManager().removeIfAuthenticated(e.getPlayer());
    }

}