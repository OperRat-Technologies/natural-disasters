package me.tcklpl.naturaldisaster.events;

import me.tcklpl.naturaldisaster.GameStatus;
import me.tcklpl.naturaldisaster.NaturalDisaster;
import me.tcklpl.naturaldisaster.player.cPlayer.CPlayer;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.Objects;

public class Move implements Listener {

    @EventHandler
    public void onMove(PlayerMoveEvent e) {

        if (!NaturalDisaster.getAuthenticationManager().isAuthenticated(e.getPlayer())) {
            e.setCancelled(true);
            Player p = e.getPlayer();
            CPlayer cp = NaturalDisaster.getPlayerManager().getCPlayer(e.getPlayer().getUniqueId());
            if (cp.getPassword() == null) {
                p.sendMessage(ChatColor.RED + "Você precisa se registrar, para isso use: /register <senha> <senha denovo>");
            } else {
                p.sendMessage(ChatColor.RED + "Você precisa se logar, para isso use: /login <senha>");
            }
        }
        else
        if (NaturalDisaster.getGameManager().getCurrentStatus() == GameStatus.STARTING) {
            Location l = e.getFrom();
            Objects.requireNonNull(e.getTo()).setX(e.getFrom().getX());
            Objects.requireNonNull(e.getTo()).setZ(e.getFrom().getZ());
        }
    }
}
