package me.tcklpl.naturaldisaster.commands;

import me.tcklpl.naturaldisaster.worlds.WorldManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class WorldCommands implements CommandExecutor {

    WorldManager worldManager;

    public WorldCommands(WorldManager worldManager) {
        this.worldManager = worldManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String alias, String[] args) {
        if (!sender.isOp()) return false;
        if (cmd.getName().equalsIgnoreCase("world") || alias.equalsIgnoreCase("w")) {
            if (args.length == 0) return false;

            // World creation
            if (args[0].equalsIgnoreCase("create")) {
                if (args.length != 3) return false;
                if (args[1].equalsIgnoreCase("void")) {
                    String worldName = args[2];
                    if (worldManager.createVoidWorld(worldName)) {
                        sender.sendMessage(ChatColor.GREEN + "Success");
                    } else sender.sendMessage(ChatColor.RED + "Failed to create world");
                    return true;
                }
            }

            // World teleportation
            if (args[0].equalsIgnoreCase("tp")) {
                if (args.length != 2) return false;
                String worldName = args[1];
                if (sender instanceof Player) {
                    Player p = (Player) sender;
                    if (worldManager.teleportPlayer(p, worldName)) {
                        p.sendMessage(ChatColor.GREEN + "Success");
                    } else {
                        p.sendMessage(ChatColor.RED + "Failed to teleport to world '" + worldName + "'");
                    }
                    return true;
                }
            }
        }
        return false;
    }
}
