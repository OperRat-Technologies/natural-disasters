package me.tcklpl.naturaldisaster.util

import org.bukkit.entity.Player

object PlayerUtils {

    fun healPlayer(p: Player) {
        p.health = 20.0
        p.foodLevel = 20
        p.fireTicks = 0
        p.fallDistance = 0f
        for (pe in p.activePotionEffects) p.removePotionEffect(pe.type)
    }
}
