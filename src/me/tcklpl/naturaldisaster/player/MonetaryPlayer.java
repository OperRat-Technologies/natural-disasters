package me.tcklpl.naturaldisaster.player;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

public class MonetaryPlayer {

    private UUID uniqueId;
    private double money;

    public MonetaryPlayer(Player p, double money) {
        this.uniqueId = p.getUniqueId();
        this.money = money;
    }

    public Player getPlayer() {
        return Bukkit.getPlayer(uniqueId);
    }

    public double getMoney() {
        return money;
    }

    public void setMoney(double money) {
        this.money = money;
    }
}
