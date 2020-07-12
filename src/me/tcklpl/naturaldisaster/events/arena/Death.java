package me.tcklpl.naturaldisaster.events.arena;

import me.tcklpl.naturaldisaster.NaturalDisaster;
import me.tcklpl.naturaldisaster.map.MapManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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

                if (NaturalDisaster.getMapManager().getPlayerMap(p) != null)
                    NaturalDisaster.getMapManager().updateArenaForDeadPlayer(p);
                else {
                    p.teleport(Objects.requireNonNull(Bukkit.getWorld("void")).getSpawnLocation());
                }

                p.setHealth(20);
                p.setFoodLevel(20);
                p.setFallDistance(0);
                p.setFireTicks(0);

                p.sendMessage(ChatColor.GRAY + ">> Você morrreu levando " + e.getDamage() / 2 + " corações de dano.");

                if (p.getActivePotionEffects().size() > 0)
                    for (PotionEffect effect : p.getActivePotionEffects())
                        p.removePotionEffect(effect.getType());

            }
        }
    }
}
