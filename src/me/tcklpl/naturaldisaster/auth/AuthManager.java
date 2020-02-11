package me.tcklpl.naturaldisaster.auth;

import org.bukkit.entity.Player;

import java.util.List;

public class AuthManager {

    private HashingManager hashingManager;
    private List<Player> authenticatedPlayers;

    public AuthManager() {
        hashingManager = new HashingManager();
    }

    public HashingManager getHashingManager() {
        return hashingManager;
    }


}
