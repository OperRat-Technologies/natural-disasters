package me.tcklpl.naturaldisaster.events

import me.tcklpl.naturaldisaster.GameStatus
import me.tcklpl.naturaldisaster.NaturalDisaster
import me.tcklpl.naturaldisaster.player.cPlayer.CPlayer
import org.bukkit.ChatColor
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

object JoinEvent : Listener {

    @EventHandler
    fun onLogin(e: PlayerJoinEvent) {
        val p = e.player

        // Register custom player
        if (NaturalDisaster.instance.cPlayerManager.getCPlayer(p.uniqueId) == null) {
            val cp = CPlayer(p.uniqueId, p.name, 0, 50.0)
            if (!NaturalDisaster.instance.cPlayerManager.registerCPlayer(cp)) NaturalDisaster.instance.logger
                .warning("Falha ao registrar player " + p.name)
            p.sendMessage(ChatColor.GREEN.toString() + "Bem-vindo ao servidor!")
        } else {
            p.sendMessage(ChatColor.GREEN.toString() + "Bem-vindo de volta!")
        }

        // Set player a spectator if the hame has already started
        if (NaturalDisaster.instance.gameManager.currentStatus != GameStatus.IN_LOBBY) {
            NaturalDisaster.instance.gameManager.teleportSpectatorToArena(p)
            p.sendMessage(ChatColor.GRAY.toString() + "O jogo já está em andamento, você jogará na próxima partida.")
        }

    }
}
