package me.tcklpl.naturaldisaster.commands;

import me.tcklpl.naturaldisaster.schematics.SchematicManager;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SchematicCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String alias, String[] args) {
        if (sender instanceof Player) {
            Player p = (Player) sender;
            if (p.isOp()) {
                if (cmd.getName().equalsIgnoreCase("sch")) {
                    if (args.length > 0) {
                        switch (args[0]) {
                            case "create":
                                if (args.length != 2) return false;
                                String name = args[1];
                                SchematicManager.getInstance().setTempName(name);
                                p.sendMessage(ChatColor.YELLOW + "Iniciada a criação do schematic " + name);
                                break;
                            case "pos1":
                                if (args.length != 1) return false;
                                Location loc = p.getLocation().getBlock().getLocation();
                                SchematicManager.getInstance().setTempPos1(loc);
                                p.sendMessage(ChatColor.YELLOW + "Definida posição 1 do schematic em (" + loc.getX() + " " + loc.getY() + " " + loc.getZ() + ")");
                                break;
                            case "pos2":
                                if (args.length != 1) return false;
                                Location loc2 = p.getLocation().getBlock().getLocation();
                                SchematicManager.getInstance().setTempPos2(loc2);
                                p.sendMessage(ChatColor.YELLOW + "Definida posição 1 do schematic em (" + loc2.getX() + " " + loc2.getY() + " " + loc2.getZ() + ")");
                                break;
                            case "finalize":
                                if (args.length != 1) return false;
                                SchematicManager.getInstance().finalizeCreation(p);
                                break;
                            case "load":
                                if (args.length != 2) return false;
                                String schName = args[1];
                                int locX = p.getLocation().getBlockX();
                                int locY = p.getLocation().getBlockY();
                                int locZ = p.getLocation().getBlockZ();
                                if (SchematicManager.getInstance().loadSchematic(schName, new Location(p.getWorld(), locX, locY, locZ), true))
                                    p.sendMessage(ChatColor.GREEN + "Schematic carregado com sucesso!");
                                else p.sendMessage(ChatColor.RED + "Erro ao carregar schematic.");
                                break;
                            default:
                                return false;
                        }
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
