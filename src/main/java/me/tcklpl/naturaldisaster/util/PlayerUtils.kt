package me.tcklpl.naturaldisaster.util;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

public class PlayerUtils {

    public static void healPlayer(Player p) {
        p.setHealth(20);
        p.setFoodLevel(20);
        p.setFireTicks(0);
        p.setFallDistance(0);
        for (PotionEffect pe : p.getActivePotionEffects())
            p.removePotionEffect(pe.getType());
    }
}
