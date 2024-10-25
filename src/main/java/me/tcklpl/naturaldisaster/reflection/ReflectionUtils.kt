package me.tcklpl.naturaldisaster.reflection

import com.mojang.authlib.GameProfile
import me.tcklpl.naturaldisaster.NaturalDisaster
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket
import org.bukkit.Bukkit
import org.bukkit.craftbukkit.v1_21_R1.entity.CraftPlayer
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import java.lang.RuntimeException
import java.lang.reflect.Field
import java.util.Arrays

object ReflectionUtils {
    /**
     * Reflection way to send a packet for one player without importing nms classes.
     * @param player the player to send the packet.
     * @param packet the packet.
     */
    fun sendPacket(player: Player, packet: Packet<*>) {
        (player as CraftPlayer).handle.connection.send(packet)
    }

    /**
     * Hides and shows again player to all online players, to be used when changing skins and/or game profiles.
     * @param mainReference main reference, needed to hide and show players.
     * @param p the player to be hidden and shown.
     */
    fun updatePlayerForEveryone(mainReference: JavaPlugin, p: Player) {
        for (other in Bukkit.getOnlinePlayers()) {
            if (other.uniqueId == p.uniqueId) continue

            sendPacket(other, Packets.Play.playOutEntityDestroy(p.entityId))
            sendPacket(other, Packets.Play.playOutNamedEntitySpawn(p))
            Bukkit.getScheduler().runTask(mainReference, Runnable { other.hidePlayer(mainReference, p) })
            Bukkit.getScheduler().runTaskLater(mainReference, Runnable { other.showPlayer(mainReference, p) }, 5)
        }
    }

    fun getPlayerGameProfile(p: Player): GameProfile {
        return (p as CraftPlayer).handle.gameProfile
    }

    fun replaceGameProfileAttributes(target: GameProfile, newOne: GameProfile) {
        Arrays.stream<Field>(target.javaClass.getDeclaredFields()).forEach { field: Field ->
            field.setAccessible(true)
            try {
                val match = newOne.javaClass.getDeclaredField(field.name)
                match.setAccessible(true)
                field.set(target, match.get(newOne))
                match.setAccessible(false)
            } catch (e: NoSuchFieldException) {
                throw RuntimeException(e)
            } catch (e: IllegalAccessException) {
                throw RuntimeException(e)
            }
            field.setAccessible(false)
        }
    }

    fun cloneGameProfile(p: Player): GameProfile {
        val gp = getPlayerGameProfile(p)
        val newGp = GameProfile(gp.id, gp.name)
        newGp.properties.putAll(gp.properties)
        return newGp
    }

    fun setPlayerGameProfile(p: Player, gp: GameProfile) {
        sendPacket(p, Packets.Play.playerInfoRemove(p))
        p.setDisplayName(gp.name)
        p.setPlayerListName(gp.name)
        replaceGameProfileAttributes(getPlayerGameProfile(p), gp)
        sendPacket(p, Packets.Play.playOutPlayerInfo(ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER, p))
        updatePlayerForEveryone(NaturalDisaster.instance, p)
    }
}
