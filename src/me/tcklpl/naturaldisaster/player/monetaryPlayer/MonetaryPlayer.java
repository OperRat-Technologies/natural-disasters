package me.tcklpl.naturaldisaster.player.monetaryPlayer;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

public class MonetaryPlayer {

    private UUID uniqueId;
    private PlayerData playerData;

    public MonetaryPlayer(UUID uuid, PlayerData playerData) {
        this.uniqueId = uuid;
        this.playerData = playerData;
    }

    public PlayerData getPlayerData() {
        return playerData;
    }

    public Player getPlayer() {
        return Bukkit.getPlayer(uniqueId);
    }

    public UUID getUUID() {
        return uniqueId;
    }
}
