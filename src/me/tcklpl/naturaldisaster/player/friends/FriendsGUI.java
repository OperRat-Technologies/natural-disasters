package me.tcklpl.naturaldisaster.player.friends;

import me.tcklpl.naturaldisaster.NaturalDisaster;
import me.tcklpl.naturaldisaster.player.cPlayer.CPlayer;
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

import java.util.Objects;
import java.util.UUID;

public class FriendsGUI implements CommandExecutor, Listener {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String alias, String[] args) {
        if (sender instanceof Player) {
            Player p = (Player) sender;
            if (cmd.getName().equalsIgnoreCase("friends") || alias.equalsIgnoreCase("amigos")) {
                CPlayer cPlayer = NaturalDisaster.getPlayerManager().getCPlayer(p.getUniqueId());
                if (args.length == 0) {
                    openFriendsGUI(p);
                    return true;
                }
                else {
                    if (args.length == 2) {
                        if (args[0].equalsIgnoreCase("add")) {
                            String requestName = args[1];
                            try {
                                Player request = Bukkit.getPlayer(requestName);
                                assert request != null;
                                if (cPlayer.getPlayerData().sendFriendRequest(request.getUniqueId())) {
                                    p.sendMessage(ChatColor.GREEN + "Pedido enviado");
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
        if (!(e.getWhoClicked() instanceof Player)) return;
        Player p = (Player) e.getWhoClicked();
        CPlayer cPlayer = NaturalDisaster.getPlayerManager().getCPlayer(p.getUniqueId());
        if (e.getView().getTitle().equalsIgnoreCase("Amigos")) {
            e.setCancelled(true);
            if (e.getCurrentItem() != null && e.getCurrentItem().getType() == Material.LIME_DYE) {
                Inventory i = Bukkit.createInventory(p, 9 + 9 * Math.floorDiv(cPlayer.getPlayerData().getFriendRequests().size(), 9), "Requests");
                for (UUID uuid : cPlayer.getPlayerData().getFriendRequests()) {
                    ItemStack is = new ItemStack(Material.PLAYER_HEAD);
                    ItemMeta im = is.getItemMeta();
                    assert im != null;
                    im.setDisplayName(NaturalDisaster.getPlayerManager().getCPlayer(uuid).getPlayerData().getName());
                    is.setItemMeta(im);
                    i.addItem(is);
                }
                p.closeInventory();
                p.openInventory(i);
            } else {
                if (e.getCurrentItem() != null && (e.getCurrentItem().getType() == Material.PLAYER_HEAD || e.getCurrentItem().getType() == Material.SKELETON_SKULL)) {
                    String name = ChatColor.stripColor(Objects.requireNonNull(e.getCurrentItem().getItemMeta()).getDisplayName());

                    Inventory i = Bukkit.createInventory(p, 27, "Opções para " + name);

                    ItemStack del = new ItemStack(Material.RED_CONCRETE);
                    ItemMeta delM = del.getItemMeta();
                    assert delM != null;
                    delM.setDisplayName(ChatColor.RED + "Excluir amigo");
                    del.setItemMeta(delM);

                    ItemStack block = new ItemStack(Material.BARRIER);
                    ItemMeta blockM = block.getItemMeta();
                    assert blockM != null;
                    blockM.setDisplayName(ChatColor.RED + "Bloquear amigo");
                    block.setItemMeta(blockM);

                    i.setItem(11, del);
                    i.setItem(15, block);

                    p.closeInventory();
                    p.openInventory(i);
                }
            }
        } else {
            if (e.getView().getTitle().equalsIgnoreCase("Requests")) {
                e.setCancelled(true);
                if (e.getCurrentItem() != null && e.getCurrentItem().getType() == Material.PLAYER_HEAD) {
                    String name = Objects.requireNonNull(e.getCurrentItem().getItemMeta()).getDisplayName();
                    CPlayer request = NaturalDisaster.getPlayerManager().getCPlayer(name);

                    Inventory i = Bukkit.createInventory(p, 9, "Pedido de amizade de " + request.getPlayerData().getName());

                    ItemStack yes = new ItemStack(Material.LIME_CONCRETE);
                    ItemMeta yesD = yes.getItemMeta();
                    assert yesD != null;
                    yesD.setDisplayName(ChatColor.GREEN + "Aceitar");
                    yes.setItemMeta(yesD);

                    ItemStack no = new ItemStack(Material.RED_CONCRETE);
                    ItemMeta noD = no.getItemMeta();
                    assert noD != null;
                    noD.setDisplayName(ChatColor.RED + "Negar");
                    no.setItemMeta(noD);

                    ItemStack block = new ItemStack(Material.BARRIER);
                    ItemMeta blockM = block.getItemMeta();
                    assert blockM != null;
                    blockM.setDisplayName(ChatColor.RED + "Bloquear");
                    block.setItemMeta(blockM);

                    i.setItem(2, yes);
                    i.setItem(4, no);
                    i.setItem(6, block);

                    p.closeInventory();
                    p.openInventory(i);
                }
            } else {
                if (e.getView().getTitle().contains("Pedido de amizade de ")) {
                    e.setCancelled(true);

                    String name = e.getView().getTitle().replace("Pedido de amizade de ", "");
                    CPlayer request = NaturalDisaster.getPlayerManager().getCPlayer(name);

                    if (e.getCurrentItem() != null && e.getCurrentItem().getType() == Material.LIME_CONCRETE) {
                        if (cPlayer.getPlayerData().acceptFriend(request.getUuid()))
                            p.sendMessage(ChatColor.GREEN + "Pedido aceito");
                        else p.sendMessage(ChatColor.RED + "Falha ao aceitar pedido.");
                        p.closeInventory();
                        openFriendsGUI(p);
                    } else if (e.getCurrentItem() != null && e.getCurrentItem().getType() == Material.RED_CONCRETE) {
                        if (cPlayer.getPlayerData().removeFriendRequest(request.getUuid()))
                            p.sendMessage(ChatColor.GREEN + "Pedido recusado");
                        else p.sendMessage(ChatColor.RED + "Falha ao recusar pedido.");
                        p.closeInventory();
                        openFriendsGUI(p);
                    } else if (e.getCurrentItem() != null && e.getCurrentItem().getType() == Material.BARRIER) {
                        if (cPlayer.getPlayerData().blockPlayer(request.getUuid()))
                            p.sendMessage(ChatColor.GREEN + "Jogador bloqueado");
                        else p.sendMessage(ChatColor.RED + "Falha ao bloquear jogador.");
                        p.closeInventory();
                        openFriendsGUI(p);
                    }
                } else {
                    if (e.getView().getTitle().contains("Opções para ")) {
                        e.setCancelled(true);

                        String name = e.getView().getTitle().replace("Opções para ", "");
                        CPlayer target = NaturalDisaster.getPlayerManager().getCPlayer(name);

                        if (e.getCurrentItem() != null && e.getCurrentItem().getType() == Material.RED_CONCRETE) {
                            if (cPlayer.getPlayerData().removeFriend(target.getUuid()))
                                p.sendMessage(ChatColor.GREEN + "Amigo excluído");
                            else p.sendMessage(ChatColor.RED + "Falha ao excluir amigo.");
                            p.closeInventory();
                            openFriendsGUI(p);
                        } else if (e.getCurrentItem() != null && e.getCurrentItem().getType() == Material.BARRIER) {
                            if (cPlayer.getPlayerData().blockPlayer(target.getUuid()))
                                p.sendMessage(ChatColor.GREEN + "Jogador bloqueado");
                            else p.sendMessage(ChatColor.RED + "Falha ao bloquear jogador.");
                            p.closeInventory();
                            openFriendsGUI(p);
                        }
                    }
                }
            }
        }
    }

    private void openFriendsGUI(Player p) {
        CPlayer cPlayer = NaturalDisaster.getPlayerManager().getCPlayer(p.getUniqueId());
        Inventory i = Bukkit.createInventory(p, 18 + 9 * Math.floorDiv(cPlayer.getPlayerData().getFriends().size(), 9), "Amigos");
        if (cPlayer.getPlayerData().getFriends().size() > 0) {
            int index = 0;
            for (UUID uniqueId : cPlayer.getPlayerData().getFriends()) {
                ItemStack is;
                Material mat;
                ChatColor nameColor;
                try {
                    if (Bukkit.getOnlinePlayers().contains(Bukkit.getPlayer(uniqueId))) {
                        mat = Material.PLAYER_HEAD;
                        nameColor = ChatColor.GREEN;
                    } else {
                        mat = Material.SKELETON_SKULL;
                        nameColor = ChatColor.GRAY;
                    }
                } catch (Exception e) {
                    mat = Material.SKELETON_SKULL;
                    nameColor = ChatColor.GRAY;
                }
                is = new ItemStack(mat);
                ItemMeta im = is.getItemMeta();
                assert im != null;
                String friendName = NaturalDisaster.getPlayerManager().getCPlayer(uniqueId).getPlayerData().getName();
                im.setDisplayName(nameColor + friendName);
                is.setItemMeta(im);
                i.setItem(9 + index++, is);
            }
        } else {
            ItemStack is = new ItemStack(Material.REDSTONE);
            ItemMeta im = is.getItemMeta();
            assert im != null;
            im.setDisplayName(ChatColor.RED + "Você não tem amigos.");
            is.setItemMeta(im);
            i.setItem(13, is);
        }
        ItemStack is = new ItemStack(cPlayer.getPlayerData().getFriendRequests().size() > 0 ? Material.LIME_DYE : Material.GRAY_DYE);
        ItemMeta im = is.getItemMeta();
        assert im != null;
        im.setDisplayName(cPlayer.getPlayerData().getFriendRequests().size() > 0 ?
                        (cPlayer.getPlayerData().getFriendRequests().size() == 1 ?
                                ChatColor.GREEN + "1 pedido de amizade pendente":
                ChatColor.GREEN + "" + cPlayer.getPlayerData().getFriendRequests().size() + " pedidos de amizade pendentes"):
                ChatColor.GRAY + "Sem pedidos de amizade pendentes.");
        is.setItemMeta(im);
        i.setItem(8, is);
        p.openInventory(i);
    }
}
