package me.tcklpl.naturaldisaster.commands;

import me.tcklpl.naturaldisaster.map.MapManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public class Start implements CommandExecutor {

    JavaPlugin main;
    public Start(JavaPlugin main) {
        this.main = main;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String alias, String[] args) {
        if (cmd.getName().equalsIgnoreCase("start") && sender.isOp()) {
            if (!MapManager.getInstance().isIsInGame()) {
                sender.sendMessage(ChatColor.GREEN + "Começando o jogo...");
                MapManager.getInstance().randomNextMap();

                Bukkit.getScheduler().scheduleSyncDelayedTask(main, MapManager.getInstance()::startNextGame, 60L);

            } else {
                sender.sendMessage(ChatColor.RED + "O jogo já está rolando");
            }
            return true;
        }
        return false;
    }
}
