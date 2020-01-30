package me.tcklpl.naturaldisaster.player.friends;

import me.tcklpl.naturaldisaster.player.monetaryPlayer.CustomPlayerManager;
import me.tcklpl.naturaldisaster.player.monetaryPlayer.MonetaryPlayer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.UUID;

public class FriendsGUI implements CommandExecutor, Listener {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String alias, String[] args) {
        if (sender instanceof Player) {
            Player p = (Player) sender;
            if (cmd.getName().equalsIgnoreCase("friends") || alias.equalsIgnoreCase("amigos")) {
                MonetaryPlayer mp = CustomPlayerManager.getInstance().getMonetaryPlayer(p.getUniqueId());
                if (args.length == 0) {
                    Inventory i = Bukkit.createInventory(p, 9 + 9 * Math.floorDiv(mp.getPlayerData().getFriends().size(), 9), "Amigos");
                    if (mp.getPlayerData().getFriends().size() > 0) {
                        for (UUID uniqueId : mp.getPlayerData().getFriends()) {
                            ItemStack is;
                            Material mat;
                            ChatColor nameColor;
                            try {
                                Bukkit.getPlayer(uniqueId);
                                mat = Material.PLAYER_HEAD;
                                nameColor = ChatColor.GREEN;
                            } catch (Exception e) {
                                mat = Material.SKELETON_SKULL;
                                nameColor = ChatColor.GRAY;
                            }
                            is = new ItemStack(mat);
                            ItemMeta im = is.getItemMeta();
                            assert im != null;
                            String friendName = CustomPlayerManager.getInstance().getMonetaryPlayer(uniqueId).getPlayerData().getName();
                            im.setDisplayName(nameColor + friendName);
                            is.setItemMeta(im);
                            i.addItem(is);
                        }
                    } else {
                        ItemStack is = new ItemStack(Material.REDSTONE);
                        ItemMeta im = is.getItemMeta();
                        assert im != null;
                        im.setDisplayName(ChatColor.RED + "Você não tem amigos.");
                        is.setItemMeta(im);
                        i.setItem(4, is);
                    }
                    ItemStack is = new ItemStack(mp.getPlayerData().getFriendRequests().size() > 0 ? Material.GREEN_DYE : Material.GRAY_DYE);
                    ItemMeta im = is.getItemMeta();
                    assert im != null;
                    im.setDisplayName(mp.getPlayerData().getFriendRequests().size() > 0 ?
                            ChatColor.GREEN + "" + mp.getPlayerData().getFriendRequests().size() + "Pedidos de amizade pendentes" :
                            ChatColor.GRAY + "Sem pedidos de amizade pendentes.");
                    is.setItemMeta(im);
                    i.setItem(i.getSize() - 1, is);
                    p.openInventory(i);
                    return true;
                }
                else {
                    if (args.length == 2) {
                        if (args[0].equalsIgnoreCase("add")) {
                            String requestName = args[1];
                            try {
                                Player request = Bukkit.getPlayer(requestName);
                                assert request != null;
                                if (CustomPlayerManager.getInstance().getMonetaryPlayer(p.getUniqueId()).getPlayerData().sendFriendRequest(request.getUniqueId())) {
                                    p.sendMessage(ChatColor.GREEN + "Pedido enviado com sucesso!");
                                } else p.sendMessage(ChatColor.RED + "Falha oa enviar pedido. Você já o enviou?");
                                return true;
                            } catch (NullPointerException e) {
                                p.sendMessage(ChatColor.RED + "Só é possível mandar requests para players que estão online.");
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    @EventHandler
    public void onFriendsGUIClick(InventoryClickEvent e) {
        if (e.getView().getTitle().equalsIgnoreCase("Amigos"))
            e.setCancelled(true);
    }
}
