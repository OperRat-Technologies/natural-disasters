package me.tcklpl.naturaldisaster.events;

import me.tcklpl.naturaldisaster.map.MapManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffect;

public class Death implements Listener {

    @EventHandler
    public void onDeath(EntityDamageEvent e) {

        if (e.getEntity() instanceof Player) {
            Player p = (Player) e.getEntity();
            if (p.getHealth() - e.getDamage() <= 0) {
                e.setCancelled(true);
                MapManager.getInstance().updateArenaForDeadPlayer(p.getName());

                p.setHealth(20);

                if (p.getActivePotionEffects().size() > 0)
                    for (PotionEffect effect : p.getActivePotionEffects())
                        p.removePotionEffect(effect.getType());
                p.setFallDistance(0);
            }
        }
    }
}
