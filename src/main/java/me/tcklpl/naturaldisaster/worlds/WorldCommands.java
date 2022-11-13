package me.tcklpl.naturaldisaster.worlds;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Objects;

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
                if (sender instanceof Player p) {
                    if (worldManager.teleportPlayer(p, worldName)) {
                        p.sendMessage(ChatColor.GREEN + "Success");
                    } else {
                        p.sendMessage(ChatColor.RED + "Failed to teleport to world '" + worldName + "'");
                    }
                    return true;
                }
            }

            if (args[0].equalsIgnoreCase("list")) {
                if (args.length != 1) return false;
                sender.sendMessage(ChatColor.GRAY + "Mundos carregados atualmente:");
                StringBuilder msg = new StringBuilder(ChatColor.GRAY + "");
                for (World w : Bukkit.getWorlds()) {
                    msg.append(w.getName());
                    msg.append(" ");
                }
                sender.sendMessage(msg.toString());
                return true;
            }
            // World unloading
            if (args[0].equalsIgnoreCase("unload")) {
                if (args.length != 2) return false;
                String worldName = args[1];

                try {
                    if (Bukkit.unloadWorld(Objects.requireNonNull(Bukkit.getWorld(worldName)), false))
                        sender.sendMessage(ChatColor.GREEN + "Mundo descarregado");
                    else sender.sendMessage(ChatColor.RED + "Falha ao descarregar mundo");
                } catch (Exception e) {
                    sender.sendMessage(ChatColor.RED + "Falha ao descarregar mundo");
                }
                return true;
            }
        }
        return false;
    }
}
