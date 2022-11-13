package me.tcklpl.naturaldisaster.auth;

import me.tcklpl.naturaldisaster.NaturalDisaster;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class AuthManager {

    private final HashingManager hashingManager;
    private final List<Player> authenticatedPlayers;

    public AuthManager() {
        hashingManager = new HashingManager();
        authenticatedPlayers = new ArrayList<>();
    }

    public HashingManager getHashingManager() {
        return hashingManager;
    }

    public boolean authPlayer(Player p) {
        NaturalDisaster.getPlayerManager().getCPlayer(p.getUniqueId()).updateOnDatabase();
        return authenticatedPlayers.add(p);
    }

    public boolean isAuthenticated(Player p) {
        return authenticatedPlayers.contains(p);
    }

    public void removeIfAuthenticated(Player p) {
        authenticatedPlayers.remove(p);
    }


}
