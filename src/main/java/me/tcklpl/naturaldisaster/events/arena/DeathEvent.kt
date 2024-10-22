package me.tcklpl.naturaldisaster.events.arena

import me.tcklpl.naturaldisaster.NaturalDisaster
import me.tcklpl.naturaldisaster.util.PlayerUtils
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.EntityEffect
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageEvent
import kotlin.math.ceil

object DeathEvent : Listener {

    @EventHandler
    fun onDeath(e: EntityDamageEvent) {
        var p = e.entity
        if (p !is Player) return

        // If the player died from the damage
        if (p.health - e.damage <= 0) {
            e.isCancelled = true
            p.playEffect(EntityEffect.HURT_EXPLOSION)

            // Register player death if we're ingame
            if (NaturalDisaster.getGameManager().isIngame) {
                NaturalDisaster.getGameManager().registerPlayerDeath(p)
            } else {
                p.teleport(Bukkit.getWorld("void")!!.spawnLocation)
            }

            PlayerUtils.healPlayer(p)
            p.sendMessage("${ChatColor.GRAY}>> Você morrreu levando ${ceil(e.damage / 2)} corações de dano.")
        }

    }
}
