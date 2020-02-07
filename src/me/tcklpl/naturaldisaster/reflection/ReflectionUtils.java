package me.tcklpl.naturaldisaster.reflection;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.InvocationTargetException;

public class ReflectionUtils {

    public static void sendPacket(Player player, Object packet) {
        try {
            Object handle = player.getClass().getMethod("getHandle").invoke(player);
            Object playerConnection = handle.getClass().getField("playerConnection").get(handle);
            playerConnection.getClass().getMethod("sendPacket", getNMSClass("Packet")).invoke(playerConnection, packet);
        } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException | NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    public static void updatePlayerForEveryone(JavaPlugin mainReference, Player p) {
        try {
            Object entityHuman = p.getClass().getMethod("getHandle").invoke(p);
            for (final Player others : Bukkit.getOnlinePlayers()) {
                if (!others.getUniqueId().equals(p.getUniqueId())) {
                    sendPacket(others, Packets.Play.PlayOutEntityDestroy(p.getEntityId()));
                    sendPacket(others, Packets.Play.PlayOutNamedEntitySpawn(entityHuman));
                    Bukkit.getScheduler().runTask(mainReference, () -> others.hidePlayer(mainReference, p));
                    Bukkit.getScheduler().runTaskLater(mainReference, () -> others.showPlayer(mainReference, p), 5);
                }
            }
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    public static Class<?> getNMSClass(String name) {
        String version = Bukkit.getServer().getClass().getPackageName().split("\\.")[3];
        try {
            return Class.forName("net.minecraft.server." + version + "." + name);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

}
