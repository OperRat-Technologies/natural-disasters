package me.tcklpl.naturaldisaster.auth;

import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;
import me.tcklpl.naturaldisaster.NaturalDisaster;
import me.tcklpl.naturaldisaster.player.cPlayer.CPlayer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.LinkedList;
import java.util.Objects;
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

    public int getQueueSize() {
        return queue.size();
    }

    private void computeHash() {
        if (queue.size() > 0)
            Bukkit.getScheduler().runTaskAsynchronously(NaturalDisaster.getMainReference(), queue.remove());
    }

    public void acceptTask(HashingService service) {
        if (service.hashData.option == HashingOption.HASH) {
            CPlayer cp = NaturalDisaster.getPlayerManager().getCPlayer(service.hashData.player.getUniqueId());
            cp.setPassword(service.hashData.hash);
            if (NaturalDisaster.getAuthenticationManager().authPlayer(service.hashData.player))
                service.hashData.player.sendMessage(ChatColor.GREEN + "Autenticado com sucesso.");
            else service.hashData.player.sendMessage(ChatColor.RED + "Falha ao autenticar jogador");
        } else if (service.hashData.option == HashingOption.COMPARE) {
            if (service.success) {
                if (NaturalDisaster.getAuthenticationManager().authPlayer(service.hashData.player))
                    service.hashData.player.sendMessage(ChatColor.GREEN + "Autenticado com sucesso.");
                else service.hashData.player.sendMessage(ChatColor.RED + "Falha ao autenticar jogador");
            } else service.hashData.player.sendMessage(ChatColor.RED + "A senha está incorreta.");
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

    public boolean isInQueue(HashData hashData) {
        return queue.contains(new HashingService(this, hashData));
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

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            HashData hashData = (HashData) o;
            return Objects.equals(player, hashData.player);
        }

        @Override
        public int hashCode() {
            return Objects.hash(player);
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
