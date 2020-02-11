package me.tcklpl.naturaldisaster.reflection;

import org.bukkit.Chunk;
import org.bukkit.entity.Player;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;

/**
 * Class with packets used in the plugin, used to call all packets using reflection and to keep normal classes cleaner
 */
public class Packets {

    public static class Play {

        public enum PlayerInfoEnum {
            REMOVE_PLAYER, ADD_PLAYER
        }

        public enum ChatMessageType {
            CHAT, SYSTEM, GAME_INFO
        }

        public static Object PlayOutPlayerInfo(PlayerInfoEnum playerInfoEnum, Player player) {
            try {
                Object entityPlayer = player.getClass().getMethod("getHandle").invoke(player);

                Object originalEnumPlayerInfo = Objects.requireNonNull(ReflectionUtils.getNMSClass("PacketPlayOutPlayerInfo$EnumPlayerInfoAction")).getField(playerInfoEnum.name()).get(null);

                Class<?> constructorArray = Array.newInstance(ReflectionUtils.getNMSClass("EntityPlayer"), 0).getClass();
                Constructor<?> playerPlayOutInfo = Objects.requireNonNull(ReflectionUtils.getNMSClass("PacketPlayOutPlayerInfo"))
                        .getConstructor(
                                ReflectionUtils.getNMSClass("PacketPlayOutPlayerInfo$EnumPlayerInfoAction"),
                                constructorArray
                        );
                Object entityPlayerArray = Array.newInstance(ReflectionUtils.getNMSClass("EntityPlayer"), 1);
                Array.set(entityPlayerArray, 0, entityPlayer);

                return playerPlayOutInfo.newInstance(originalEnumPlayerInfo, entityPlayerArray);

            } catch (IllegalAccessException | NoSuchFieldException | NoSuchMethodException | InvocationTargetException | InstantiationException e) {
                e.printStackTrace();
                return null;
            }

        }

        /**
         * Generates a packet for entity despawn
         * @param entityId the id of the entity
         * @return the generated packet
         */
        public static Object PlayOutEntityDestroy(int entityId) {
            try {
                Constructor<?> entityDestroy = Objects.requireNonNull(ReflectionUtils.getNMSClass("PacketPlayOutEntityDestroy")).getConstructor(int[].class);
                return entityDestroy.newInstance((Object) new int[] {entityId});
            } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
                e.printStackTrace();
                return null;
            }
        }

        /**
         * Generates a packet for human spawn, to be used for updating game profiles or NPCs
         * @param entityHuman the entity to be spawned
         * @return the generated packet
         */
        public static Object PlayOutNamedEntitySpawn(Object entityHuman) {
            try {
                Constructor<?> entitySpawn = Objects.requireNonNull(ReflectionUtils.getNMSClass("PacketPlayOutNamedEntitySpawn")).getConstructor(ReflectionUtils.getNMSClass("EntityHuman"));
                return entitySpawn.newInstance(entityHuman);
            } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
                e.printStackTrace();
                return null;
            }
        }

        public static Object PlayOutMapChunk(Chunk chunkToUpdate) {
            try {
                Object nmsChunk = chunkToUpdate.getClass().getMethod("getHandle").invoke(chunkToUpdate);
                Constructor<?> mapChunk = Objects.requireNonNull(ReflectionUtils.getNMSClass("PacketPlayOutMapChunk"))
                        .getConstructor(
                                ReflectionUtils.getNMSClass("Chunk"),
                                int.class
                        );
                return mapChunk.newInstance(nmsChunk, 65535);
            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException | InstantiationException e) {
                e.printStackTrace();
                return null;
            }
        }

        public static Object PlayOutChat(String text, ChatMessageType type) {
            try {
                Constructor<?> packet = Objects.requireNonNull(ReflectionUtils.getNMSClass("PacketPlayOutChat"))
                        .getConstructor(
                                ReflectionUtils.getNMSClass("IChatBaseComponent"),
                                ReflectionUtils.getNMSClass("ChatMessageType")
                        );
                Class<?> chatBaseComponentClass = Objects.requireNonNull(ReflectionUtils.getNMSClass("IChatBaseComponent$ChatSerializer"));
                Method target = chatBaseComponentClass.getDeclaredMethod("a", String.class);

                Object chatBaseComponent = target.invoke(null, text);
                Object originalEnum = Objects.requireNonNull(ReflectionUtils.getNMSClass("ChatMessageType")).getField(type.name()).get(null);

                return packet.newInstance(chatBaseComponent, originalEnum);

            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | NoSuchFieldException | InstantiationException e) {
                e.printStackTrace();
                return null;
            }
        }

    }

}
