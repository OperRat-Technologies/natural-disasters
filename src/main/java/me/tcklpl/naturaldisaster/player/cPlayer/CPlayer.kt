package me.tcklpl.naturaldisaster.player.cPlayer;

import java.io.Serializable;
import java.util.UUID;

public class CPlayer implements Serializable {

    private final PlayerData playerData;
    private final UUID uuid;

    public CPlayer(UUID uuid, PlayerData playerData) {
        this.uuid = uuid;
        this.playerData = playerData;
    }

    public PlayerData getPlayerData() {
        return playerData;
    }

    public UUID getUuid() {
        return uuid;
    }

}
