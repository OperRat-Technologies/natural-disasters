package me.tcklpl.naturaldisaster.player.monetaryPlayer;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class PlayerData implements Serializable {

    private int wins, hints, respawns;
    private double money;
    private List<UUID> friends, friendRequests, blocks;
    private UUID playerUUID;
    private String name;
    private boolean modified;

    public PlayerData(String name, int wins, int hints, int respawns, double money, List<UUID> friends, List<UUID> friendRequests, List<UUID> blocks) {
        this.name = name;
        this.wins = wins;
        this.hints = hints;
        this.respawns = respawns;
        this.money = money;
        this.friends = friends;
        this.friendRequests = friendRequests;
        this.blocks = blocks;
        modified = false;
    }

    public void setPlayerUUID(UUID playerUUID) {
        this.playerUUID = playerUUID;
        modified = true;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        modified = true;
    }

    public int getWins() {
        return wins;
    }

    public void setWins(int wins) {
        this.wins = wins;
        modified = true;
    }

    public int getHints() {
        return hints;
    }

    public void setHints(int hints) {
        this.hints = hints;
        modified = true;
    }

    public int getRespawns() {
        return respawns;
    }

    public void setRespawns(int respawns) {
        this.respawns = respawns;
        modified = true;
    }

    public double getMoney() {
        return money;
    }

    public void setMoney(double money) {
        this.money = money;
        modified = true;
    }

    public List<UUID> getFriends() {
        return friends;
    }

    public void setFriends(List<UUID> friends) {
        this.friends = friends;
        modified = true;
    }

    public List<UUID> getFriendRequests() {
        return friendRequests;
    }

    public boolean addFriendRequest(UUID friendRequest) {
        if (this.friendRequests.contains(friendRequest) || this.blocks.contains(friendRequest) || friends.contains(friendRequest)) return false;
        friendRequests.add(friendRequest);
        Objects.requireNonNull(Bukkit.getPlayer(playerUUID)).
                sendMessage(ChatColor.GRAY + Objects.requireNonNull(Bukkit.getPlayer(friendRequest)).getName() + " te enviou um pedido de amizade.");
        modified = true;
        return true;
    }

    public void forceAddFriend(UUID uuid) {
        if (!friends.contains(uuid))
            friends.add(uuid);
        modified = true;
    }

    public boolean acceptFriend(UUID name) {
        if (!friendRequests.contains(name)) return false;
        if (friends.contains(name)) return false;
        friendRequests.remove(name);
        friends.add(name);
        CustomPlayerManager.getInstance().getMonetaryPlayer(name).getPlayerData().forceAddFriend(this.playerUUID);
        modified = true;
        return true;
    }

    public boolean sendFriendRequest(UUID uuid) {
        if (friendRequests.contains(uuid))
            return acceptFriend(uuid);
        if (friends.contains(uuid)) return false;
        modified = true;
        return CustomPlayerManager.getInstance().getMonetaryPlayer(uuid).getPlayerData().addFriendRequest(this.playerUUID);
    }

    public boolean removeFriendRequest(UUID uuid) {
        if (friendRequests.contains(uuid))
            return friendRequests.remove(uuid);
        modified = true;
        return false;
    }

    public boolean blockPlayer(UUID uuid) {
        modified = true;
        if (friendRequests.remove(uuid) || friends.remove(uuid)) {
            return blocks.add(uuid);
        }
        return false;
    }

    public boolean removeBlock(UUID uuid) {
        modified = true;
        if (blocks.contains(uuid))
            return blocks.remove(uuid);
        return false;
    }

    public List<UUID> getBlocks() {
        return blocks;
    }

    public boolean removeFriend(UUID uuid) {
        modified = true;
        return friends.remove(uuid);
    }

    public boolean isModified() { return modified; }
}
