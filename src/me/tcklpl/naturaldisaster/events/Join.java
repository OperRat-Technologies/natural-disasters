package me.tcklpl.naturaldisaster.events;

import me.tcklpl.naturaldisaster.GameStatus;
import me.tcklpl.naturaldisaster.map.MapManager;
import me.tcklpl.naturaldisaster.player.monetaryPlayer.CustomPlayerManager;
import me.tcklpl.naturaldisaster.player.monetaryPlayer.MonetaryPlayer;
import me.tcklpl.naturaldisaster.player.monetaryPlayer.PlayerData;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.ArrayList;

public class Join implements Listener {

    @EventHandler
    public void onLogin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        if (CustomPlayerManager.getInstance().getMonetaryPlayer(p.getUniqueId()) == null) {
            PlayerData playerData = new PlayerData(p.getName(), 0, 0, 0, 50, new ArrayList<>(), new ArrayList<>());
            playerData.setPlayerUUID(p.getUniqueId());
            MonetaryPlayer mp = new MonetaryPlayer(p.getUniqueId(), playerData);
            CustomPlayerManager.getInstance().registerPlayer(mp);
            p.sendMessage(ChatColor.GREEN + "Bem-vindo ao servidor!");
        } else {
            p.sendMessage(ChatColor.GREEN + "Bem-vindo de volta!");
        }
        if (MapManager.getInstance().getCurrentStatus() != GameStatus.IN_LOBBY) {
            MapManager.getInstance().teleportSpectatorToArena(p);
            p.sendMessage(ChatColor.GRAY + "O jogo já está em andamento, você jogará na próxima partida.");
        }
    }
}
