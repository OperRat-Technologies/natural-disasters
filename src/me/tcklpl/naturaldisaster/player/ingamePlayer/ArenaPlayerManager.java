package me.tcklpl.naturaldisaster.player.ingamePlayer;

import com.mojang.authlib.GameProfile;
import me.tcklpl.naturaldisaster.player.ingamePlayer.ArenaPlayer;
import net.minecraft.server.v1_15_R1.EntityLiving;
import net.minecraft.server.v1_15_R1.PacketPlayOutEntityDestroy;
import net.minecraft.server.v1_15_R1.PacketPlayOutNamedEntitySpawn;
import net.minecraft.server.v1_15_R1.PacketPlayOutPlayerInfo;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.UUID;

public class ArenaPlayerManager {

    private HashMap<UUID, ArenaPlayer> arenaPlayers;
    private JavaPlugin main;

    public ArenaPlayerManager(JavaPlugin main) {
        this.arenaPlayers = new HashMap<>();
        this.main = main;
    }

    public ArenaPlayer getPlayer(Player p) {
        return arenaPlayers.get(p.getUniqueId());
    }

    public void setGameProfile(Player p, EntityLiving entityLiving, boolean disguising) {
        try {
            Field gp2 = entityLiving.getClass().getSuperclass().getDeclaredField("bT");
            gp2.setAccessible(true);
            if (disguising)
                gp2.set(entityLiving, getPlayer(p).getOldProfile());
            else
                gp2.set(entityLiving, getPlayer(p).getNewProfile());
            gp2.setAccessible(false);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
            if (getPlayer(p) != null) {
                arenaPlayers.remove(p.getUniqueId());
            }
        }
    }

    public void disguisePlayer(final Player p, String name) {
        if (getPlayer(p) != null) {
            Bukkit.getLogger().warning("Usuário " + p.getName() + " já tranformado");
            return;
        }
        CraftPlayer cp = ((CraftPlayer) p);
        GameProfile profile = new GameProfile(p.getUniqueId(), name);
        EntityLiving entityLiving = cp.getHandle();
        arenaPlayers.put(p.getUniqueId(), new ArenaPlayer(p, name, cp.getProfile(), profile));
        p.setDisplayName(name);
        p.setPlayerListName(name);

        // remove the player
        cp.getHandle().playerConnection.sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, cp.getHandle()));

        setGameProfile(p, entityLiving, false);

        // add the player
        cp.getHandle().playerConnection.sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, cp.getHandle()));

        for (final Player others : Bukkit.getOnlinePlayers()) {
            if (!others.getUniqueId().equals(p.getUniqueId())) {
                ((CraftPlayer) others).getHandle().playerConnection.sendPacket(new PacketPlayOutEntityDestroy(p.getEntityId()));
                ((CraftPlayer) others).getHandle().playerConnection.sendPacket(new PacketPlayOutNamedEntitySpawn(cp.getHandle()));

                Bukkit.getScheduler().runTask(main, () -> others.hidePlayer(main, p));
                Bukkit.getScheduler().runTaskLater(main, () -> others.showPlayer(main, p), 5);
            }
        }
    }

    public void returnPlayerToNormal(Player p) {
        if (arenaPlayers.containsKey(p.getUniqueId())) {

            CraftPlayer cp = ((CraftPlayer) p);
            EntityLiving entityLiving = cp.getHandle();

            // remove the player
            cp.getHandle().playerConnection.sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, cp.getHandle()));

            setGameProfile(p, entityLiving, true);

            // add the player
            cp.getHandle().playerConnection.sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, cp.getHandle()));

            for (final Player others : Bukkit.getOnlinePlayers()) {
                if (!others.getUniqueId().equals(p.getUniqueId())) {
                    ((CraftPlayer) others).getHandle().playerConnection.sendPacket(new PacketPlayOutEntityDestroy(p.getEntityId()));
                    ((CraftPlayer) others).getHandle().playerConnection.sendPacket(new PacketPlayOutNamedEntitySpawn(cp.getHandle()));

                    Bukkit.getScheduler().runTask(main, () -> others.hidePlayer(main, p));
                    Bukkit.getScheduler().runTaskLater(main, () -> others.showPlayer(main, p), 5);
                }
            }


        }
    }
}
