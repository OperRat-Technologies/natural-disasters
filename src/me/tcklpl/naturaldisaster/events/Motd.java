package me.tcklpl.naturaldisaster.events;

import me.tcklpl.naturaldisaster.NaturalDisaster;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerListPingEvent;

public class Motd implements Listener {

    @EventHandler
    public void onServerListPink(ServerListPingEvent e) {

        switch (NaturalDisaster.getGameManager().getCurrentStatus()) {
            case IN_LOBBY:
                e.setMotd(ChatColor.GREEN + "Em lobby");
                break;
            case STARTING:
                e.setMotd(ChatColor.YELLOW + "Iniciando...");
                break;
            case IN_GAME:
                e.setMotd(ChatColor.RED + "Em jogo");
                break;
        }
    }
}
