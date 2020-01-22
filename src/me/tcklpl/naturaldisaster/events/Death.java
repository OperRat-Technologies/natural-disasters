package me.tcklpl.naturaldisaster.events;

import me.tcklpl.naturaldisaster.map.MapManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffect;

import java.util.Objects;

public class Death implements Listener {

    @EventHandler
    public void onDeath(EntityDamageEvent e) {

        if (e.getEntity() instanceof Player) {
            Player p = (Player) e.getEntity();
            if (p.getHealth() - e.getDamage() <= 0) {
                e.setCancelled(true);

                if (MapManager.getInstance().getPlayerMap(p.getName()) != null)
                    MapManager.getInstance().updateArenaForDeadPlayer(p.getName());
                else {
                    p.teleport(Objects.requireNonNull(Bukkit.getWorld("void")).getSpawnLocation());
                }

                p.setHealth(20);
                p.setFoodLevel(20);
                p.setFallDistance(0);
                p.setFireTicks(0);

                if (p.getActivePotionEffects().size() > 0)
                    for (PotionEffect effect : p.getActivePotionEffects())
                        p.removePotionEffect(effect.getType());

            }
        }
    }
}
