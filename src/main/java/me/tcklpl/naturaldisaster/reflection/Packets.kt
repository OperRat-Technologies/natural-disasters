package me.tcklpl.naturaldisaster.reflection;

import net.minecraft.network.protocol.game.*;
import net.minecraft.server.level.ServerEntity;
import org.bukkit.Chunk;
import org.bukkit.craftbukkit.v1_21_R1.CraftChunk;
import org.bukkit.craftbukkit.v1_21_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.BitSet;
import java.util.Collections;
import java.util.Set;

/**
 * Class with packets used in the plugin, used to call all packets using reflection and to keep normal classes cleaner
 */
public class Packets {

    public static class Play {

        public static ClientboundPlayerInfoUpdatePacket PlayOutPlayerInfo(ClientboundPlayerInfoUpdatePacket.Action playerInfoEnum, Player player) {
            return new ClientboundPlayerInfoUpdatePacket(playerInfoEnum, ((CraftPlayer) player).getHandle());
        }

        public static ClientboundPlayerInfoRemovePacket PlayerInfoRemove(Player p) {
            return new ClientboundPlayerInfoRemovePacket(Collections.singletonList(p.getUniqueId()));
        }

        public static ClientboundRemoveEntitiesPacket PlayOutEntityDestroy(int entityId) {
            return new ClientboundRemoveEntitiesPacket(entityId);
        }

        public static ClientboundAddEntityPacket PlayOutNamedEntitySpawn(Player p) {
            var cp = (CraftPlayer) p;
            var sp = cp.getHandle();
            var serverEntity = new ServerEntity(sp.serverLevel(), sp, 0, false, packet -> {}, Set.of());

            return new ClientboundAddEntityPacket(cp.getHandle(), serverEntity);
        }

        public static ClientboundLevelChunkWithLightPacket PlayOutMapChunk(Chunk chunkToUpdate) {
            var levelChunk = ((CraftChunk) chunkToUpdate).getCraftWorld().getHandle().getLevel().getChunk(chunkToUpdate.getX(), chunkToUpdate.getZ());
            return new ClientboundLevelChunkWithLightPacket(levelChunk, levelChunk.getLevel().getLightEngine(), new BitSet(), new BitSet());
        }

    }

}
