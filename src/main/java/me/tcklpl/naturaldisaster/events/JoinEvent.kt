package me.tcklpl.naturaldisaster.events

import me.tcklpl.naturaldisaster.GameStatus
import me.tcklpl.naturaldisaster.NaturalDisaster
import me.tcklpl.naturaldisaster.player.cPlayer.CPlayer
import me.tcklpl.naturaldisaster.player.cPlayer.PlayerData
import me.tcklpl.naturaldisaster.util.SkinUtils
import org.bukkit.ChatColor
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import java.util.ArrayList
import java.util.UUID

object JoinEvent : Listener {

    @EventHandler
    fun onLogin(e: PlayerJoinEvent) {
        val p = e.player

        // Register custom player
        if (NaturalDisaster.getPlayerManager().getCPlayer(p.uniqueId) == null) {
            val playerData =
                PlayerData(p.name, 0, 0, 0, 50.0, ArrayList<UUID>(), ArrayList<UUID>(), ArrayList<UUID>())
            playerData.setPlayerUUID(p.uniqueId)
            val cp = CPlayer(p.uniqueId, playerData)
            if (!NaturalDisaster.getPlayerManager().registerCPlayer(cp)) NaturalDisaster.getMainReference().logger
                .warning("Falha ao registrar player " + p.name)
            p.sendMessage(ChatColor.GREEN.toString() + "Bem-vindo ao servidor!")
        } else {
            p.sendMessage(ChatColor.GREEN.toString() + "Bem-vindo de volta!")
        }

        // Set player a sspectator if the hame has already started
        if (NaturalDisaster.getGameManager().currentStatus != GameStatus.IN_LOBBY) {
            NaturalDisaster.getGameManager().teleportSpectatorToArena(p)
            p.sendMessage(ChatColor.GRAY.toString() + "O jogo já está em andamento, você jogará na próxima partida.")
        }

        // Download or apply player skins
        if (!NaturalDisaster.getSkinManager().isRegistered(p.name)) {
            val uuidStr = SkinUtils.getOriginalUUIDString(p)
            if (uuidStr != null) {
                NaturalDisaster.getSkinManager().addPlayerToSkinQueue(p, uuidStr)
            }
        } else {
            SkinUtils.applySkin(
                NaturalDisaster.getMainReference(),
                p,
                NaturalDisaster.getSkinManager().getSkin(p.name)
            )
        }
    }
}
