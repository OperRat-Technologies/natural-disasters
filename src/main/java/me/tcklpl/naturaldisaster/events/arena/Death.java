package me.tcklpl.naturaldisaster.events.arena;

import me.tcklpl.naturaldisaster.NaturalDisaster;
import me.tcklpl.naturaldisaster.util.PlayerUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.EntityEffect;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffect;

import java.util.Objects;

public class Death implements Listener {

    @EventHandler
    public void onDeath(EntityDamageEvent e) {

        if (e.getEntity() instanceof Player p) {
            if (p.getHealth() - e.getDamage() <= 0) {
                e.setCancelled(true);
                p.playEffect(EntityEffect.HURT);

                if (NaturalDisaster.getGameManager().isIngame())
                    NaturalDisaster.getGameManager().registerPlayerDeath(p);
                else {
                    p.teleport(Objects.requireNonNull(Bukkit.getWorld("void")).getSpawnLocation());
                }

                PlayerUtils.healPlayer(p);

                p.sendMessage(ChatColor.GRAY + ">> Você morrreu levando " + e.getDamage() / 2 + " corações de dano.");

                if (p.getActivePotionEffects().size() > 0)
                    for (PotionEffect effect : p.getActivePotionEffects())
                        p.removePotionEffect(effect.getType());

            }
        }
    }
}
