package me.tcklpl.naturaldisaster.player.cPlayer;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.UUID;

public class CPlayer implements Serializable {

    private String password;
    private Timestamp lastPasswordChange;
    private PlayerData playerData;
    private UUID uuid;

    public CPlayer(UUID uuid, String password, Timestamp lastPasswordChange, PlayerData playerData) {
        this.uuid = uuid;
        this.password = password;
        this.lastPasswordChange = lastPasswordChange;
        this.playerData = playerData;
    }

    public String getPassword() {
        return password;
    }

    public Timestamp getLastPasswordChange() {
        return lastPasswordChange;
    }

    public PlayerData getPlayerData() {
        return playerData;
    }

    public void setPassword(String password) {
        this.password = password;
        this.lastPasswordChange = new Timestamp(System.currentTimeMillis());
    }

    public UUID getUuid() {
        return uuid;
    }
}
