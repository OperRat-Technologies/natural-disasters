package me.tcklpl.naturaldisaster.player.ingamePlayer

import com.mojang.authlib.GameProfile
import org.bukkit.entity.Player
import java.util.UUID

data class ArenaPlayer(
    val player: Player,
    val arenaName: String,
    val oldProfile: GameProfile,
    val newProfile: GameProfile
) {
    val realName: String = player.name
    val uuid: UUID = player.uniqueId
}
