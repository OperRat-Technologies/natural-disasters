package me.tcklpl.naturaldisaster.admin;

import me.tcklpl.naturaldisaster.GameStatus;
import me.tcklpl.naturaldisaster.NaturalDisaster;
import me.tcklpl.naturaldisaster.disasters.Disaster;
import me.tcklpl.naturaldisaster.map.DisasterMap;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class ArenaAdmin implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String alias, String[] args) {
        if (sender.isOp()) {
            if (!(sender instanceof Player)) return false;
            Player p = (Player) sender;
            if (cmd.getName().equalsIgnoreCase("admin")) {
                if (args.length != 2) return false;

                if (args[0].equalsIgnoreCase("set")) {

                    if (NaturalDisaster.getMapManager().getCurrentStatus() != GameStatus.IN_LOBBY) {
                        sender.sendMessage(ChatColor.RED + "Só é possível definir mapa e desastre quando estiver em lobby, se o jogo já acabou aguarde alguns segundos.");
                        return true;
                    }

                    if (args[1].equalsIgnoreCase("map")) {

                        List<DisasterMap> maps = NaturalDisaster.getMapManager().getAllMaps();
                        int size = 9 + 9 * Math.floorDiv(maps.size() - 1, 9);
                        Inventory i = Bukkit.createInventory(p, size, "Admin Map Selection");
                        for (DisasterMap map : maps) {
                            ItemStack is = new ItemStack(Material.GRASS_BLOCK);
                            ItemMeta im = is.getItemMeta();
                            assert im != null;
                            im.setDisplayName(map.getName());
                            is.setItemMeta(im);
                            i.addItem(is);
                        }
                        p.openInventory(i);
                        return true;

                    }

                    if (args[1].equalsIgnoreCase("disaster")) {

                        List<Disaster> disasters = NaturalDisaster.getMapManager().getAllDisasters();
                        int size = 9 + 9 * Math.floorDiv(disasters.size() - 1, 9);
                        Inventory i = Bukkit.createInventory(p, size, "Admin Disaster Selection");
                        for (Disaster disaster : disasters) {
                            ItemStack is = new ItemStack(disaster.getIcon());
                            ItemMeta im = is.getItemMeta();
                            assert im != null;
                            im.setDisplayName(ChatColor.WHITE + disaster.getName());
                            List<String> lore = new ArrayList<>();
                            lore.add("Playable: " + (disaster.isPlayable() ? ChatColor.GREEN + "YES" : ChatColor.RED + "NO"));
                            im.setLore(lore);
                            is.setItemMeta(im);
                            i.addItem(is);
                        }
                        p.openInventory(i);
                        return true;

                    }

                }

            }
        }
        return false;
    }
}
