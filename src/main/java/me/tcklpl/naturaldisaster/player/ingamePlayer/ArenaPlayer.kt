package me.tcklpl.naturaldisaster.player.ingamePlayer;

import com.mojang.authlib.GameProfile;
import org.bukkit.entity.Player;

import java.util.UUID;

public class ArenaPlayer {

    private final Player player;
    private final String realName, arenaName;
    private final UUID uuid;

    private final GameProfile oldProfile, newProfile;

    public ArenaPlayer(Player player, String arenaName, GameProfile oldProfile, GameProfile newProfile) {
        this.player = player;
        this.realName = player.getName();
        this.arenaName = arenaName;
        this.uuid = player.getUniqueId();
        this.oldProfile = oldProfile;
        this.newProfile = newProfile;
    }

    public Player getPlayer() {
        return player;
    }

    public String getRealName() {
        return realName;
    }

    public String getArenaName() {
        return arenaName;
    }

    public UUID getUUID() {
        return uuid;
    }

    public GameProfile getOldProfile() {
        return oldProfile;
    }

    public GameProfile getNewProfile() {
        return newProfile;
    }
}
