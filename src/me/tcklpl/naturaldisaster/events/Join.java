package me.tcklpl.naturaldisaster.events;

import me.tcklpl.naturaldisaster.GameStatus;
import me.tcklpl.naturaldisaster.NaturalDisaster;
import me.tcklpl.naturaldisaster.map.MapManager;
import me.tcklpl.naturaldisaster.player.cPlayer.CPlayer;
import me.tcklpl.naturaldisaster.player.cPlayer.PlayerData;
import me.tcklpl.naturaldisaster.player.skins.SkinManager;
import me.tcklpl.naturaldisaster.util.SkinUtils;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;

public class Join implements Listener {

    private final JavaPlugin main;
    public Join(JavaPlugin main) {
        this.main = main;
    }

    @EventHandler
    public void onLogin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        if (NaturalDisaster.getPlayerManager().getCPlayer(p.getUniqueId()) == null) {
            PlayerData playerData = new PlayerData(p.getName(), 0, 0, 0, 50, new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
            playerData.setPlayerUUID(p.getUniqueId());
            CPlayer cp = new CPlayer(p.getUniqueId(), null, null, playerData);
            if (!NaturalDisaster.getPlayerManager().registerCPlayer(cp))
                NaturalDisaster.getMainReference().getLogger().warning("Falha ao registrar player " + p.getName());
            p.sendMessage(ChatColor.GREEN + "Bem-vindo ao servidor!");
        } else {
            p.sendMessage(ChatColor.GREEN + "Bem-vindo de volta!");
        }
        if (NaturalDisaster.getMapManager().getCurrentStatus() != GameStatus.IN_LOBBY) {
            NaturalDisaster.getMapManager().teleportSpectatorToArena(p);
            p.sendMessage(ChatColor.GRAY + "O jogo já está em andamento, você jogará na próxima partida.");
        }
        if (!NaturalDisaster.getSkinManager().isRegistered(p.getName())) {
            String uuidStr = SkinUtils.getOriginalUUIDString(p);
            if (uuidStr != null) {
                NaturalDisaster.getSkinManager().addPlayerToSkinQueue(p, uuidStr);
            }
        } else {
            SkinUtils.applySkin(main, p, NaturalDisaster.getSkinManager().getSkin(p.getName()));
        }
    }
}
