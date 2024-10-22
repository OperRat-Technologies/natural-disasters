package me.tcklpl.naturaldisaster.events.arena

import me.tcklpl.naturaldisaster.NaturalDisaster
import org.bukkit.ChatColor
import org.bukkit.GameMode
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageEvent

object DamageEvent : Listener {

    @EventHandler
    fun onDamage(e: EntityDamageEvent) {
        var p = e.entity
        if (p !is Player) return

        if (p.gameMode == GameMode.ADVENTURE) {
            // Don't damage players if we're in lobby
            if (!NaturalDisaster.getGameManager().isIngame) e.isCancelled = true
            // Ingame players can get fucked
            // Kill players that fell from the arena
            else if (e.cause == EntityDamageEvent.DamageCause.VOID && NaturalDisaster.getGameManager()
                    .currentMap.playersInArena.contains(p)
            ) {
                NaturalDisaster.getGameManager().registerPlayerDeath(p)
                p.sendMessage(ChatColor.GRAY.toString() + ">> Você morreu por cair da arena.")
            }
        }

    }
}
