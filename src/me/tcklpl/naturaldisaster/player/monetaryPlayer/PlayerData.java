package me.tcklpl.naturaldisaster.player.monetaryPlayer;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class PlayerData {

    private int wins, hints, respawns;
    private double money;
    private List<UUID> friends, friendRequests, blocks;
    private UUID playerUUID;
    private String name;

    public PlayerData(String name, int wins, int hints, int respawns, double money, List<UUID> friends, List<UUID> friendRequests, List<UUID> blocks) {
        this.name = name;
        this.wins = wins;
        this.hints = hints;
        this.respawns = respawns;
        this.money = money;
        this.friends = friends;
        this.friendRequests = friendRequests;
        this.blocks = blocks;
    }

    public void setPlayerUUID(UUID playerUUID) {
        this.playerUUID = playerUUID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getWins() {
        return wins;
    }

    public void setWins(int wins) {
        this.wins = wins;
    }

    public int getHints() {
        return hints;
    }

    public void setHints(int hints) {
        this.hints = hints;
    }

    public int getRespawns() {
        return respawns;
    }

    public void setRespawns(int respawns) {
        this.respawns = respawns;
    }

    public double getMoney() {
        return money;
    }

    public void setMoney(double money) {
        this.money = money;
    }

    public List<UUID> getFriends() {
        return friends;
    }

    public void setFriends(List<UUID> friends) {
        this.friends = friends;
    }

    public List<UUID> getFriendRequests() {
        return friendRequests;
    }

    public boolean addFriendRequest(UUID friendRequest) {
        if (this.friendRequests.contains(friendRequest) || this.blocks.contains(friendRequest) || friends.contains(friendRequest)) return false;
        friendRequests.add(friendRequest);
        Objects.requireNonNull(Bukkit.getPlayer(playerUUID)).
                sendMessage(ChatColor.GRAY + Objects.requireNonNull(Bukkit.getPlayer(friendRequest)).getName() + " te enviou um pedido de amizade.");
        return true;
    }

    public void forceAddFriend(UUID uuid) {
        if (!friends.contains(uuid))
            friends.add(uuid);
    }

    public boolean acceptFriend(UUID name) {
        if (!friendRequests.contains(name)) return false;
        if (friends.contains(name)) return false;
        friendRequests.remove(name);
        friends.add(name);
        CustomPlayerManager.getInstance().getMonetaryPlayer(name).getPlayerData().forceAddFriend(this.playerUUID);
        return true;
    }

    public boolean sendFriendRequest(UUID uuid) {
        if (friendRequests.contains(uuid))
            return acceptFriend(uuid);
        if (friends.contains(uuid)) return false;
        return CustomPlayerManager.getInstance().getMonetaryPlayer(uuid).getPlayerData().addFriendRequest(this.playerUUID);
    }

    public boolean removeFriendRequest(UUID uuid) {
        if (friendRequests.contains(uuid))
            return friendRequests.remove(uuid);
        return false;
    }

    public boolean blockPlayer(UUID uuid) {
        if (friendRequests.remove(uuid) || friends.remove(uuid)) {
            return blocks.add(uuid);
        }
        return false;
    }

    public boolean removeBlock(UUID uuid) {
        if (blocks.contains(uuid))
            return blocks.remove(uuid);
        return false;
    }

    public List<UUID> getBlocks() {
        return blocks;
    }

    public boolean removeFriend(UUID uuid) {
        return friends.remove(uuid);
    }
}
