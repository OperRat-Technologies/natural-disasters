package me.tcklpl.naturaldisaster.player.cPlayer;

import me.tcklpl.naturaldisaster.NaturalDisaster;
import me.tcklpl.naturaldisaster.database.Database;

import java.io.Serializable;
import java.sql.SQLException;
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

    public void updateOnDatabase() {
        try {
            Database db = NaturalDisaster.getDatabase();
            db.update("players",
                    new String[] {"uuid", "name", "password", "last_pwd_change", "wins", "hints", "respawns", "money"},
                    new Object[] {uuid.toString(), playerData.getName(), password, lastPasswordChange, playerData.getWins(), playerData.getHints(), playerData.getRespawns(), playerData.getMoney()},
                    new String[] {"uuid"},
                    new Object[] {uuid.toString()});
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void insertOnDatabase() {
        try {
            Database db = NaturalDisaster.getDatabase();
            db.insert("players",
                    new String[] {"uuid", "name", "password", "last_pwd_change", "wins", "hints", "respawns", "money"},
                    new Object[] {uuid.toString(), playerData.getName(), password, lastPasswordChange, playerData.getWins(), playerData.getHints(), playerData.getRespawns(), playerData.getMoney()});
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
