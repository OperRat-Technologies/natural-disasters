package me.tcklpl.naturaldisaster.reflection;

import net.minecraft.network.protocol.game.ClientboundAddPlayerPacket;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import org.bukkit.Chunk;
import org.bukkit.craftbukkit.v1_19_R1.CraftChunk;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.BitSet;

/**
 * Class with packets used in the plugin, used to call all packets using reflection and to keep normal classes cleaner
 */
public class Packets {

    public static class Play {

        public static ClientboundPlayerInfoPacket PlayOutPlayerInfo(ClientboundPlayerInfoPacket.Action playerInfoEnum, Player player) {
            return new ClientboundPlayerInfoPacket(playerInfoEnum, ((CraftPlayer) player).getHandle());
        }

        public static ClientboundRemoveEntitiesPacket PlayOutEntityDestroy(int entityId) {
            return new ClientboundRemoveEntitiesPacket(entityId);
        }

        public static ClientboundAddPlayerPacket PlayOutNamedEntitySpawn(Player p) {
            return new ClientboundAddPlayerPacket(((CraftPlayer)p).getHandle());
        }

        public static ClientboundLevelChunkWithLightPacket PlayOutMapChunk(Chunk chunkToUpdate) {
            var levelChunk = ((CraftChunk) chunkToUpdate).getHandle();
            return new ClientboundLevelChunkWithLightPacket(levelChunk, levelChunk.getLevel().getLightEngine(), new BitSet(), new BitSet(), true);
//            PacketContainer mapChunk = new PacketContainer(PacketType.Play.Server.MAP_CHUNK);
//            mapChunk.get
//            new ClientboundLevelChunkPacketData(chunkToUpdate.)
//            try {
//                Object nmsChunk = chunkToUpdate.getClass().getMethod("getHandle").invoke(chunkToUpdate);
//                Constructor<?> mapChunk = Objects.requireNonNull(ReflectionUtils.getNMSClass("PacketPlayOutMapChunk"))
//                        .getConstructor(
//                                ReflectionUtils.getNMSClass("Chunk"),
//                                int.class
//                        );
//                //return mapChunk.newInstance(nmsChunk, 65535, true);
//                return mapChunk.newInstance(nmsChunk, 65535);
//            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException | InstantiationException e) {
//                e.printStackTrace();
//                return null;
//            }
        }

    }

}
