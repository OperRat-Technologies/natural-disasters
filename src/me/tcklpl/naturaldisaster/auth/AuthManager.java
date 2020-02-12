package me.tcklpl.naturaldisaster.auth;

import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class AuthManager {

    private HashingManager hashingManager;
    private List<Player> authenticatedPlayers;

    public AuthManager() {
        hashingManager = new HashingManager();
        authenticatedPlayers = new ArrayList<>();
    }

    public HashingManager getHashingManager() {
        return hashingManager;
    }

    public boolean authPlayer(Player p) {
        return authenticatedPlayers.add(p);
    }

    public boolean isAuthenticated(Player p) {
        return authenticatedPlayers.contains(p);
    }

    public void removeIfAuthenticated(Player p) {
        authenticatedPlayers.remove(p);
    }


}
