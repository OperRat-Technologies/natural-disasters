package me.tcklpl.naturaldisaster.reflection;

import org.bukkit.Bukkit;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class ReflectionUtils {

    public static enum PrecipitationType {
        NONE, RAIN, SNOW, ALL;
    }

    /**
     * Reflection way to send a packet for one player without importing nms classes.
     * @param player the player to send the packet.
     * @param packet the packet.
     */
    public static void sendPacket(Player player, Object packet) {
        try {
            Object handle = player.getClass().getMethod("getHandle").invoke(player);
            Object playerConnection = handle.getClass().getField("playerConnection").get(handle);
            playerConnection.getClass().getMethod("sendPacket", getNMSClass("Packet")).invoke(playerConnection, packet);
        } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException | NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    /**
     * Hides and shows again player to all online players, to be used when changing skins and/or game profiles.
     * @param mainReference main reference, needed to hide and show players.
     * @param p the player to be hidden and shown.
     */
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

    /**
     * Fetches the current server version number and gets the requested class by name.
     * @param name the name of the class.
     * @return the fetched class.
     */
    public static Class<?> getNMSClass(String name) {
        String version = Bukkit.getServer().getClass().getPackageName().split("\\.")[3];
        try {
            return Class.forName("net.minecraft.server." + version + "." + name);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Gets a list of all biomes in wich the precipitation type equals the one given.
     * @param precipitationType the requested precicipation type.
     * @return the list of matching biomes.
     */
    public static List<Biome> getListOfRequiredPrecipitationBiomes(PrecipitationType precipitationType) {

        Biome[] allBukkitBiomes = Biome.values();

        if (precipitationType == PrecipitationType.ALL)
            return Arrays.asList(allBukkitBiomes);

        List<Biome> filteredFinalList = new ArrayList<>();

        for (Biome biome : allBukkitBiomes) {
            try {
                Object biomeBase = Objects.requireNonNull(getNMSClass("Biomes")).getField(biome.toString()).get(null);
                Field precipitation = biomeBase.getClass().getSuperclass().getDeclaredField("p");
                precipitation.setAccessible(true);
                if (precipitation.get(biomeBase).toString().equalsIgnoreCase(precipitationType.toString()))
                    filteredFinalList.add(biome);
                precipitation.setAccessible(false);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        return filteredFinalList;

    }

}
