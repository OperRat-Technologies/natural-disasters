package me.tcklpl.naturaldisaster.reflection;

import com.mojang.authlib.GameProfile;
import me.tcklpl.naturaldisaster.NaturalDisaster;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_21_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;

public class ReflectionUtils {

    /**
     * Reflection way to send a packet for one player without importing nms classes.
     * @param player the player to send the packet.
     * @param packet the packet.
     */
    public static void sendPacket(Player player, Packet<?> packet) {
        ((CraftPlayer) player).getHandle().connection.send(packet);
    }

    /**
     * Hides and shows again player to all online players, to be used when changing skins and/or game profiles.
     * @param mainReference main reference, needed to hide and show players.
     * @param p the player to be hidden and shown.
     */
    public static void updatePlayerForEveryone(JavaPlugin mainReference, Player p) {
        for (final Player others : Bukkit.getOnlinePlayers()) {
            if (!others.getUniqueId().equals(p.getUniqueId())) {
                sendPacket(others, Packets.Play.PlayOutEntityDestroy(p.getEntityId()));
                sendPacket(others, Packets.Play.PlayOutNamedEntitySpawn(p));
                Bukkit.getScheduler().runTask(mainReference, () -> others.hidePlayer(mainReference, p));
                Bukkit.getScheduler().runTaskLater(mainReference, () -> others.showPlayer(mainReference, p), 5);
            }
        }
    }

    public static GameProfile getPlayerGameProfile(Player p) {
        return ((CraftPlayer) p).getHandle().getGameProfile();
    }

    public static void replaceGameProfileAttributes(GameProfile target, GameProfile newOne) {
        Arrays.stream(target.getClass().getDeclaredFields()).forEach(field -> {
            field.setAccessible(true);
            try {
                var match = newOne.getClass().getDeclaredField(field.getName());
                match.setAccessible(true);

                field.set(target, match.get(newOne));

                match.setAccessible(false);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
            field.setAccessible(false);
        });
    }

    public static GameProfile cloneGameProfile(Player p) {
        var gp = getPlayerGameProfile(p);

        var newGp = new GameProfile(gp.getId(), gp.getName());
        newGp.getProperties().putAll(gp.getProperties());

        return newGp;
    }

    public static void setPlayerGameProfile(Player p, GameProfile gp) {
        sendPacket(p, Packets.Play.PlayerInfoRemove(p));
        p.setDisplayName(gp.getName());
        p.setPlayerListName(gp.getName());
        replaceGameProfileAttributes(getPlayerGameProfile(p), gp);
        sendPacket(p, Packets.Play.PlayOutPlayerInfo(ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER, p));
        updatePlayerForEveryone(NaturalDisaster.getMainReference(), p);
    }

}
