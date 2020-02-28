package me.tcklpl.naturaldisaster.events.arena;

import me.tcklpl.naturaldisaster.GameStatus;
import me.tcklpl.naturaldisaster.NaturalDisaster;
import me.tcklpl.naturaldisaster.map.MapManager;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

public class Damage implements Listener {

    @EventHandler
    public void onDamage(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player) {
            Player p = (Player) e.getEntity();
            if (p.getGameMode() == GameMode.ADVENTURE)
                if (NaturalDisaster.getMapManager().getCurrentStatus() == GameStatus.STARTING || NaturalDisaster.getMapManager().getCurrentStatus() == GameStatus.IN_LOBBY)
                    e.setCancelled(true);
        }
    }
}
