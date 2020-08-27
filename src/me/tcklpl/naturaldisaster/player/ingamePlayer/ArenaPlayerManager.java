package me.tcklpl.naturaldisaster.player.ingamePlayer;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import me.tcklpl.naturaldisaster.NaturalDisaster;
import me.tcklpl.naturaldisaster.reflection.Packets;
import me.tcklpl.naturaldisaster.reflection.ReflectionUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
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

    /**
     * Modifies player's GameProfile to allow nametag changing
     * @param p The player whose gameprofile will be changed, Player object needed because of it's UUID
     * @param entityLiving Reference to player but as in entityLiving object form.
     * @param disguising If it will apply the disguised game profile or the real one.
     */
    public void setGameProfile(Player p, Object entityLiving, boolean disguising) {
        try {
            Field gp2 = entityLiving.getClass().getSuperclass().getDeclaredField("bJ");
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

    /**
     * Disguises player (to be used in arena).
     * @param p the player to be disguised.
     * @param name the name to be applied to said player.
     */
    public void disguisePlayer(final Player p, String name) {
        if (getPlayer(p) != null) {
            NaturalDisaster.getMainReference().getLogger().warning("Usuário " + p.getName() + " já tranformado");
            return;
        }
        GameProfile profile = new GameProfile(p.getUniqueId(), name);

        //TODO: choose textures from config
        profile.getProperties().put("textures", new Property("textures",
                "eyJ0aW1lc3RhbXAiOjE1NjI2ODg3NjMyOTEsInByb2ZpbGVJZCI6ImIwZDczMmZlMDBmNzQwN2U5ZTdmNzQ2MzAxY2Q5OG" +
                        "NhIiwicHJvZmlsZU5hbWUiOiJPUHBscyIsInNpZ25hdHVyZVJlcXVpcmVkIjp0cnVlLCJ0ZXh0dXJlcyI6eyJTS0lOIjp" +
                        "7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMWE0YWY3MTg0NTVkNGFhYjUyOGU3YTYxZ" +
                        "jg2ZmEyNWU2YTM2OWQxNzY4ZGNiMTNmN2RmMzE5YTcxM2ViODEwYiJ9fX0=",
                "QBbXXjOaNTiwdAmYPy+mfQR7O2Cs2zqmOEL+eaP+bamTZ1ujPgxMOPt/xznX1LPdTa7JDR4AWlIr4HVGNl5MdEIqqxnZ" +
                        "ALGtzDeO57wfnNz0vtu2skt+JLmLjI2687Y1I2SOo9vdNx/b81T6mUDJ1t/dk1YTjPdBJuHAhKd4c52n12mchj5JzPGI5" +
                        "0r0KVcLtAhZeeKRXO3dwOnWNHdkyMe1A4DQdP2MCpZxQgX0NJmVPTW5oDCUY5QQF5WjxxUNiHdcBG3ruwzrSxNVjjyaOe" +
                        "RlFa6jDipk9wZZzqKMFBefrZ2deTf8Z29rNJIIK0WkOoYXCGZxLNZzUHfiIwdUNggWr0ccCh6KsNIZtJQiPIdiQPXO41+" +
                        "7iWMzc9EIWzZUwqDQk2Nq+/sZlHEVnsIh2knHqaezSA97oodNPnB34WtfsN8PDoLRI6r22S4fJelgn9SFUmQUiNn1fS6t" +
                        "8hBEnjnbZcHfcjQ+O8v3RcuEubkLg2ZzV6RiJtRHYNBmhr8jHmdEoADmxf78xjbGHyXr/U8+63DB/XlXjV0RXI4oGZNkxK" +
                        "NLP+Gg+uN4qN/H+noUEYnY4/Ls58ye/EuT7JuR0X1fSenOkY3x/L79K1cqrSi3EK1MmdTOwQAdiYi9HUUcGAf0Qv6YfDt7" +
                        "UHrUcTA5F3uR66TwJmPsDjWsNTBznFE="));

        try {
            Object entityLiving = p.getClass().getMethod("getHandle").invoke(p);
            GameProfile originalProfile = (GameProfile) p.getClass().getMethod("getProfile").invoke(p);
            arenaPlayers.put(p.getUniqueId(), new ArenaPlayer(p, name, originalProfile, profile));
            p.setDisplayName(name);
            p.setPlayerListName(name);

            // remove the player
            ReflectionUtils.sendPacket(p, Packets.Play.PlayOutPlayerInfo(Packets.Play.PlayerInfoEnum.REMOVE_PLAYER, p));

            setGameProfile(p, entityLiving, false);

            // add the player
            ReflectionUtils.sendPacket(p, Packets.Play.PlayOutPlayerInfo(Packets.Play.PlayerInfoEnum.ADD_PLAYER, p));

            ReflectionUtils.updatePlayerForEveryone(main, p);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns player to normal after game, removing disguise.
     * @param p the player.
     */
    public void returnPlayerToNormal(Player p) {
        if (arenaPlayers.containsKey(p.getUniqueId())) {

            try {

                Object entityLiving = p.getClass().getMethod("getHandle").invoke(p);
                // remove the player
                ReflectionUtils.sendPacket(p, Packets.Play.PlayOutPlayerInfo(Packets.Play.PlayerInfoEnum.REMOVE_PLAYER, p));

                setGameProfile(p, entityLiving, true);

                // add the player
                ReflectionUtils.sendPacket(p, Packets.Play.PlayOutPlayerInfo(Packets.Play.PlayerInfoEnum.ADD_PLAYER, p));

                ReflectionUtils.updatePlayerForEveryone(main, p);

                arenaPlayers.remove(p.getUniqueId());
            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                e.printStackTrace();
            }

        }
    }
}
