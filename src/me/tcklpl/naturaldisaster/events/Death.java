package me.tcklpl.naturaldisaster.events;

import me.tcklpl.naturaldisaster.map.DisasterMap;
import me.tcklpl.naturaldisaster.map.MapManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public class Death implements Listener {

    JavaPlugin main;
    public Death(JavaPlugin main) {
        this.main = main;
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        Player p = e.getEntity();
        if (MapManager.getPlayerMap(p.getName()) != null) {
            p.setGameMode(GameMode.SPECTATOR);
            if (Objects.requireNonNull(MapManager.getPlayerMap(p.getName())).getPlayersInArena().size() <= 1) {
                Bukkit.getScheduler().cancelTasks(main);
                Bukkit.broadcastMessage(ChatColor.GREEN + "ACABOU");
                Objects.requireNonNull(MapManager.getPlayerMap(p.getName())).getPlayersInArena().clear();
                for (Player all : Bukkit.getOnlinePlayers()) {
                    all.teleport(new Location(Bukkit.getWorld("void"), 8, 8, 8));
                }

                Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(main, () -> {

                    if (Bukkit.unloadWorld(Objects.requireNonNull(Bukkit.getWorld("Farm")), false))
                        Bukkit.getLogger().info("Deslodou essa porra");
                    else Bukkit.getLogger().info("FALHO ESSA MERDA");

                }, 60L);


            } else {
                Objects.requireNonNull(MapManager.getPlayerMap(p.getName())).removePlayerOnArena(p.getName());
            }

        }
    }
}
