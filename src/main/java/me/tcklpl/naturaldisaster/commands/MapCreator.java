package me.tcklpl.naturaldisaster.commands;

import me.tcklpl.naturaldisaster.NaturalDisaster;
import me.tcklpl.naturaldisaster.map.DisasterMap;
import me.tcklpl.naturaldisaster.map.TempDisasterMap;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;
import java.util.stream.Collectors;

public class MapCreator implements CommandExecutor {

    private static final HashMap<Player, TempDisasterMap> tempMaps = new HashMap<>();

    public MapCreator() {

    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String alias, String[] args) {
        if (!(sender instanceof Player p)) return false;
        if (!p.isOp()) return false;

        if (cmd.getName().equalsIgnoreCase("arena")) {

            if (args.length == 0)
                return false;

            switch (args[0].toLowerCase()) {
                case "create": {
                    if (args.length < 2) return false;
                    String name = Arrays.stream(args).skip(1).collect(Collectors.joining(" "));
                    String worldName = Objects.requireNonNull(p.getLocation().getWorld()).getName();
                    if (tempMaps.containsKey(p)) {
                        p.sendMessage(ChatColor.YELLOW + "Você já está criando o mapa " + tempMaps.get(p).getName() + ", você pode cancelar a operação com /arena cancel");
                        return true;
                    }
                    tempMaps.put(p, new TempDisasterMap(name, worldName));
                    p.sendMessage(ChatColor.GREEN + "Iniciada a criação do mapa " + name);
                    return true;
                }
                case "pos1": {
                    if (args.length != 1) return false;
                    if (!tempMaps.containsKey(p)) {
                        p.sendMessage(ChatColor.RED + "Você não está criando nenhuma arena no momento.");
                        return true;
                    }
                    Location pos = p.getLocation();
                    tempMaps.get(p).setPos1(pos);
                    p.sendMessage(ChatColor.GREEN + "Definida posição 1 do mapa '" + tempMaps.get(p).getName() + "' em (" + pos.getBlockX() + " " + pos.getBlockY() + " " + pos.getBlockZ() + ")");
                    return true;
                }
                case "pos2": {
                    if (args.length != 1) return false;
                    if (!tempMaps.containsKey(p)) {
                        p.sendMessage(ChatColor.RED + "Você não está criando nenhuma arena no momento.");
                        return true;
                    }
                    Location pos = p.getLocation();
                    tempMaps.get(p).setPos2(pos);
                    p.sendMessage(ChatColor.GREEN + "Definida posição 2 do mapa '" + tempMaps.get(p).getName() + "' em (" + pos.getBlockX() + " " + pos.getBlockY() + " " + pos.getBlockZ() + ")");
                    return true;
                }
                case "spawn": {
                    if (args.length != 1) return false;
                    if (!tempMaps.containsKey(p)) {
                        p.sendMessage(ChatColor.RED + "Você não está criando nenhuma arena no momento.");
                        return true;
                    }
                    Location pos = p.getLocation();
                    tempMaps.get(p).addSpawn(pos);
                    p.sendMessage(ChatColor.GREEN + "Adicionado o " + tempMaps.get(p).getSpawns().size() + "º spawn do mapa '" + tempMaps.get(p).getName() + "' em (" + pos.getBlockX() + " " + pos.getBlockY() + " " + pos.getBlockZ() + ")");
                    return true;
                }
                case "finalize": {
                    if (args.length != 1) return false;
                    if (!tempMaps.containsKey(p)) {
                        p.sendMessage(ChatColor.RED + "Você não está criando nenhuma arena no momento.");
                        return true;
                    }
                    TempDisasterMap temp = tempMaps.get(p);
                    if (!temp.isComplete()) {
                        p.sendMessage(ChatColor.RED + "Você ainda não terminou todos os passos da criação do mapa. Você pode ver seu progresso em /arena info");
                        return true;
                    }
                    DisasterMap map = new DisasterMap(temp.getName(), temp.getWorldName(), temp.getPos1(), temp.getPos2(), temp.getSpawns(), temp.getIcon());
                    NaturalDisaster.getGameManager().getArenaManager().registerArena(map);
                    p.sendMessage(ChatColor.GREEN + "Finalizado e registrado o mapa '" + map.getName() + "'");
                    tempMaps.remove(p);
                    return true;
                }
                case "cancel": {
                    if (args.length != 1) return false;
                    if (!tempMaps.containsKey(p)) {
                        p.sendMessage(ChatColor.RED + "Você não está criando nenhuma arena no momento.");
                        return true;
                    }
                    p.sendMessage(ChatColor.GREEN + "Foi cancelada a criação do mapa '" + tempMaps.get(p).getName() + "'");
                    tempMaps.remove(p);
                    return true;
                }
                case "info": {
                    if (args.length != 1) return false;
                    if (!tempMaps.containsKey(p)) {
                        p.sendMessage(ChatColor.YELLOW + "[" + ChatColor.GREEN + "✓" + ChatColor.YELLOW + "] Você não está criando nenhuma arena no momento.");
                        return true;
                    }
                    TempDisasterMap temp = tempMaps.get(p);
                    p.sendMessage(ChatColor.YELLOW + "Criação da arena '" + temp.getName() + "': " +
                            (temp.isComplete() ? ChatColor.GREEN + "COMPLETA" : ChatColor.RED + "INCOMPLETA"));
                    p.sendMessage(ChatColor.YELLOW + "[" + (temp.getPos1() != null ? ChatColor.GREEN + "✓" : ChatColor.RED + "✕") + ChatColor.YELLOW + "] Pos 1");
                    p.sendMessage(ChatColor.YELLOW + "[" + (temp.getPos2() != null ? ChatColor.GREEN + "✓" : ChatColor.RED + "✕") + ChatColor.YELLOW + "] Pos 2");
                    p.sendMessage(ChatColor.YELLOW + "[" + (temp.getSpawns().size() >= 24 ? ChatColor.GREEN + "✓" : ChatColor.RED + "✕") +
                            ChatColor.YELLOW + "] Spawns (" + temp.getSpawns().size() + ")");
                    p.sendMessage(ChatColor.YELLOW + "[" + (temp.getIcon() != null ? ChatColor.GREEN + "✓" : ChatColor.RED + "✕") + ChatColor.YELLOW + "] Icone");
                    if (temp.isComplete()) {
                        p.sendMessage(ChatColor.GREEN + "A criação da arena está completa, você pode finalizá-la com /arena finalize");
                    }
                    return true;
                }
                case "list": {
                    if (args.length != 1) return false;
                    StringBuilder message = new StringBuilder();
                    p.sendMessage(ChatColor.GREEN + "Arenas:");
                    for (DisasterMap map : NaturalDisaster.getGameManager().getArenaManager().getArenas()) {
                        message.append(ChatColor.GRAY);
                        message.append(" - ");
                        message.append(map.getName());
                    }
                    p.sendMessage(message.toString());
                    return true;
                }
                case "icon": {
                    if (args.length != 1) return false;
                    if (!tempMaps.containsKey(p)) {
                        p.sendMessage(ChatColor.RED + "Você não está criando nenhuma arena no momento.");
                        return true;
                    }
                    if (p.getInventory().getItemInMainHand().getType() == Material.AIR) {
                        p.sendMessage(ChatColor.RED + "Você não está segurando nenhum item no momento.");
                        return true;
                    }
                    tempMaps.get(p).setIcon(p.getInventory().getItemInMainHand().getType());
                    p.sendMessage(ChatColor.GREEN + "Ícone da arena '" + tempMaps.get(p).getName() +
                            "' definido como '" + p.getInventory().getItemInMainHand().getType() + "'");
                    return true;
                }
                default:
                    return false;
            }
        }
        return false;
    }
}
