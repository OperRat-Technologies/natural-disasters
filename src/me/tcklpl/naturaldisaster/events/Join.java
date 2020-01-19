package me.tcklpl.naturaldisaster.events;

import me.tcklpl.naturaldisaster.GameStatus;
import me.tcklpl.naturaldisaster.map.MapManager;
import me.tcklpl.naturaldisaster.player.CustomPlayerManager;
import me.tcklpl.naturaldisaster.player.MonetaryPlayer;
import me.tcklpl.naturaldisaster.util.ActionBar;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class Join implements Listener {

    @EventHandler
    public void onLogin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        if (CustomPlayerManager.getInstance().getMonetaryPlayer(p.getUniqueId()) == null) {
            MonetaryPlayer mp = new MonetaryPlayer(p, 50);
            CustomPlayerManager.getInstance().registerPlayer(mp);
        } else {
            p.sendMessage(ChatColor.GREEN + "Bem-vindo de volta!");
        }
        if (MapManager.getInstance().getCurrentStatus() != GameStatus.IN_LOBBY) {
            MapManager.getInstance().teleportSpectatorToArena(p);
            p.sendMessage(ChatColor.GRAY + "O jogo já está em andamento, você jogará na próxima partida.");
        }
    }
}
