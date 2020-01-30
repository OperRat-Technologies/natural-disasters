package me.tcklpl.naturaldisaster.admin;

import me.tcklpl.naturaldisaster.disasters.Disaster;
import me.tcklpl.naturaldisaster.map.DisasterMap;
import me.tcklpl.naturaldisaster.map.MapManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.Objects;

public class AdminInventoryClick implements Listener {

    @EventHandler
    public void onInvClick(InventoryClickEvent e) {

        if (e.getWhoClicked() instanceof Player) {

            Player p = (Player) e.getWhoClicked();

            if (p.isOp()) {
                if (e.getView().getTitle().equalsIgnoreCase("Admin Map Selection")) {
                    e.setCancelled(true);
                    String mapname = Objects.requireNonNull(Objects.requireNonNull(e.getCurrentItem()).getItemMeta()).getDisplayName();
                    DisasterMap map = MapManager.getInstance().getMapByName(mapname);
                    if (map != null) {
                        MapManager.getInstance().setCurrentMap(map);
                        p.sendMessage(ChatColor.GREEN + "Mapa definido como " + ChatColor.RED + mapname);
                        p.closeInventory();
                    } else p.sendMessage(ChatColor.RED + "Erro ao definir o mapa.");
                }

                if (e.getView().getTitle().equalsIgnoreCase("Admin Disaster Selection")) {
                    e.setCancelled(true);
                    String disname = Objects.requireNonNull(Objects.requireNonNull(e.getCurrentItem()).getItemMeta()).getDisplayName();
                    Disaster disaster = MapManager.getInstance().getDisasterByName(disname);
                    if (disaster != null) {
                        MapManager.getInstance().setCurrentDisaster(disaster);
                        p.sendMessage(ChatColor.GREEN + "Desastre definido como " + ChatColor.RED + disname);
                        p.closeInventory();
                    } else p.sendMessage(ChatColor.RED + "Erro ao definir o desastre.");
                }
            }

        }

    }

}
