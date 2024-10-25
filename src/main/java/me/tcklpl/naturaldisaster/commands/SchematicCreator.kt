package me.tcklpl.naturaldisaster.commands;

import me.tcklpl.naturaldisaster.NaturalDisaster;
import me.tcklpl.naturaldisaster.schematics.Schematic;
import me.tcklpl.naturaldisaster.schematics.SchematicLoadPosition;
import me.tcklpl.naturaldisaster.schematics.SchematicManager;
import me.tcklpl.naturaldisaster.schematics.TempSchematic;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class SchematicCreator implements CommandExecutor {

    private final Map<Player, TempSchematic> schematicMap = new HashMap<>();
    private final SchematicManager schematicManager = NaturalDisaster.getSchematicManager();

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String alias, String[] args) {
        if (sender instanceof Player p) {
            if (p.isOp()) {
                if (cmd.getName().equalsIgnoreCase("sch")) {
                    if (args.length > 0) {
                        switch (args[0]) {
                            case "create": {
                                if (args.length < 2) return false;
                                String name = Arrays.stream(args).skip(1).collect(Collectors.joining(" "));
                                if (schematicMap.containsKey(p)) {
                                    p.sendMessage(ChatColor.RED + "Você já está criando um schematic no momento");
                                    return true;
                                }
                                if (!schematicManager.isNameAvailable(name)) {
                                    p.sendMessage(ChatColor.RED + "Já existe um schematic com esse nome");
                                    return true;
                                }
                                schematicMap.put(p, new TempSchematic(name));
                                p.sendMessage(ChatColor.YELLOW + "Iniciada a criação do schematic '" + name + "'");
                                break;
                            }
                            case "pos1": {
                                if (args.length != 1) return false;
                                if (!schematicMap.containsKey(p)) {
                                    p.sendMessage(ChatColor.RED + "Você não está criando nenhum schematic no momento");
                                    return true;
                                }
                                Location loc = p.getLocation().getBlock().getLocation();
                                schematicMap.get(p).setPos1(loc);
                                p.sendMessage(ChatColor.YELLOW + "Definida posição 1 do schematic em (" + loc.getX() + " " + loc.getY() + " " + loc.getZ() + ")");
                                break;
                            }
                            case "pos2": {
                                if (args.length != 1) return false;
                                if (!schematicMap.containsKey(p)) {
                                    p.sendMessage(ChatColor.RED + "Você não está criando nenhum schematic no momento");
                                    return true;
                                }
                                Location loc = p.getLocation().getBlock().getLocation();
                                schematicMap.get(p).setPos2(loc);
                                p.sendMessage(ChatColor.YELLOW + "Definida posição 2 do schematic em (" + loc.getX() + " " + loc.getY() + " " + loc.getZ() + ")");
                                break;
                            }
                            case "finalize": {
                                if (args.length != 1) return false;
                                if (!schematicMap.containsKey(p)) {
                                    p.sendMessage(ChatColor.RED + "Você não está criando nenhum schematic no momento");
                                    return true;
                                }
                                TempSchematic tempSchematic = schematicMap.get(p);
                                if (!tempSchematic.isFinished()) {
                                    p.sendMessage(ChatColor.RED + "Você ainda não definiu os 2 pontos do schematic");
                                    return true;
                                }
                                schematicManager.registerSchematic(tempSchematic.generateSchematic());
                                schematicMap.remove(p);
                                p.sendMessage(ChatColor.GREEN + "Finalizada a criação do schematic '" + tempSchematic.getName() + "'");
                                break;
                            }
                            case "load":
                                if (args.length < 2) return false;
                                String name = Arrays.stream(args).skip(1).collect(Collectors.joining(" "));
                                Schematic requested = schematicManager.getSchematicByName(name);
                                if (requested == null) {
                                    p.sendMessage(ChatColor.RED + "Não foi encontrado o schematic solicitado");
                                    return true;
                                }
                                schematicManager.loadSchematicAt(p.getLocation(), requested, false, SchematicLoadPosition.FLOOR_CENTER);
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
