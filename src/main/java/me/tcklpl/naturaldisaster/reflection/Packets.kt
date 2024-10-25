package me.tcklpl.naturaldisaster.reflection

import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket
import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket
import net.minecraft.server.level.ServerEntity
import net.minecraft.server.network.ServerPlayerConnection
import org.bukkit.Chunk
import org.bukkit.craftbukkit.v1_21_R1.CraftChunk
import org.bukkit.craftbukkit.v1_21_R1.entity.CraftPlayer
import org.bukkit.entity.Player
import java.util.BitSet
import java.util.UUID
import java.util.function.Consumer

/**
 * Class with packets used in the plugin, used to call all packets using reflection and to keep normal classes cleaner
 */
class Packets {

    object Play {
        fun playOutPlayerInfo(
            playerInfoEnum: ClientboundPlayerInfoUpdatePacket.Action,
            player: Player
        ): ClientboundPlayerInfoUpdatePacket {
            return ClientboundPlayerInfoUpdatePacket(playerInfoEnum, (player as CraftPlayer).handle)
        }

        fun playerInfoRemove(p: Player): ClientboundPlayerInfoRemovePacket {
            return ClientboundPlayerInfoRemovePacket(mutableListOf<UUID?>(p.uniqueId))
        }

        fun playOutEntityDestroy(entityId: Int): ClientboundRemoveEntitiesPacket {
            return ClientboundRemoveEntitiesPacket(entityId)
        }

        fun playOutNamedEntitySpawn(p: Player): ClientboundAddEntityPacket {
            val cp = p as CraftPlayer
            val sp = cp.handle
            val serverEntity = ServerEntity(
                sp.serverLevel(),
                sp,
                0,
                false,
                Consumer { packet: Packet<*>? -> },
                mutableSetOf<ServerPlayerConnection?>()
            )

            return ClientboundAddEntityPacket(cp.handle, serverEntity)
        }

        fun playOutMapChunk(chunkToUpdate: Chunk): ClientboundLevelChunkWithLightPacket {
            val levelChunk = (chunkToUpdate as CraftChunk).craftWorld.handle.level
                .getChunk(chunkToUpdate.x, chunkToUpdate.z)
            return ClientboundLevelChunkWithLightPacket(
                levelChunk,
                levelChunk.level.lightEngine,
                BitSet(),
                BitSet()
            )
        }
    }
}
