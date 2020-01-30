package me.tcklpl.naturaldisaster.shop;

import me.tcklpl.naturaldisaster.player.monetaryPlayer.CustomPlayerManager;
import me.tcklpl.naturaldisaster.player.monetaryPlayer.MonetaryPlayer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class Shop {

    private Player player;
    private MonetaryPlayer monetaryPlayer;

    public Shop(Player p) {
        this.player = p;
        this.monetaryPlayer = CustomPlayerManager.getInstance().getMonetaryPlayer(p.getUniqueId());
    }

    public void show() {
        Inventory i = Bukkit.createInventory(player, 27, "Shop");

        ItemStack money = new ItemStack(Material.SUNFLOWER);
        ItemMeta moneyMeta = money.getItemMeta();
        assert moneyMeta != null;
        moneyMeta.setDisplayName(ChatColor.GREEN + "$" + monetaryPlayer.getPlayerData().getMoney());
        money.setItemMeta(moneyMeta);

        ItemStack wins = new ItemStack(Material.GOLD_BLOCK);
        ItemMeta winsMeta = wins.getItemMeta();
        assert winsMeta != null;
        winsMeta.setDisplayName(ChatColor.GREEN + "Vitórias: " + monetaryPlayer.getPlayerData().getWins());
        wins.setItemMeta(winsMeta);

        i.setItem(4, money);
        i.setItem(26, wins);

        player.openInventory(i);
    }


}
