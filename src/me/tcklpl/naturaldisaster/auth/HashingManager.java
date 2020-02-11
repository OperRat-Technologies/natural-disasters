package me.tcklpl.naturaldisaster.auth;

import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;
import me.tcklpl.naturaldisaster.NaturalDisaster;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.LinkedList;
import java.util.Queue;

public class HashingManager {

    private Argon2 argon2;
    private Queue<HashingService> queue;

    public HashingManager() {
        this.argon2 = Argon2Factory.create(Argon2Factory.Argon2Types.ARGON2id);
        queue = new LinkedList<>();
    }

    public void addToQueue(HashData data) {
        queue.add(new HashingService(this, data));
        if (queue.size() == 1) {
            computeHash();
        }
    }

    private void computeHash() {
        if (queue.size() > 0)
            Bukkit.getScheduler().runTaskAsynchronously(NaturalDisaster.getMainReference(), queue.remove());
    }

    public void acceptTask(HashingService service) {
        if (service.hashData.option == HashingOption.HASH)
            try {
                NaturalDisaster.getDatabase().insert("passwords", new String[] {"uuid", "pass"}, new Object[] {service.hashData.player.getUniqueId().toString(), service.hashData.getHash()});
            } catch (SQLException e) {
                service.hashData.player.sendMessage("erro");
            }
        if (queue.size() > 0)
            computeHash();
    }

    public Argon2 getArgon2() {
        return argon2;
    }


    public enum HashingOption {
        HASH, COMPARE
    }

    public static class HashData {

        private String password, hash;
        private Player player;
        private HashingOption option;

        public HashData(Player player, HashingOption option, String password, String hash) {
            this.player = player;
            this.password = password;
            this.hash = hash;
            this.option = option;
        }

        public String getPassword() {
            return password;
        }

        public void setHash(String hash) {
            this.hash = hash;
        }

        public String getHash() {
            return hash;
        }

        public HashingOption getOption() {
            return option;
        }
    }

}
