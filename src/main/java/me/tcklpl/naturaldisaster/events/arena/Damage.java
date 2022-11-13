package me.tcklpl.naturaldisaster.events.arena;

import me.tcklpl.naturaldisaster.GameStatus;
import me.tcklpl.naturaldisaster.NaturalDisaster;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

public class Damage implements Listener {

    @EventHandler
    public void onDamage(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player p) {
            if (p.getGameMode() == GameMode.ADVENTURE)
                if (NaturalDisaster.getGameManager().getCurrentStatus() == GameStatus.STARTING || NaturalDisaster.getGameManager().getCurrentStatus() == GameStatus.IN_LOBBY)
                    e.setCancelled(true);
                else
                    if (p.getLocation().getBlockY() < 0) {
                        p.damage(20);
                        p.sendMessage(ChatColor.GRAY + ">> Você morreu por cair da arena.");
                    }
        }
    }
}
