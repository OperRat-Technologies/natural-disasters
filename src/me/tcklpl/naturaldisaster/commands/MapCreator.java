package me.tcklpl.naturaldisaster.commands;

import me.tcklpl.naturaldisaster.disasters.Disaster;
import me.tcklpl.naturaldisaster.map.DisasterMap;
import me.tcklpl.naturaldisaster.map.MapManager;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MapCreator implements CommandExecutor {

    private static DisasterMap map;

    private JavaPlugin main;
    public MapCreator(JavaPlugin main) {
        this.main = main;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String alias, String[] args) {
        if (!(sender instanceof Player)) return false;
        Player p = (Player) sender;
        if (!p.isOp()) return false;

        if (cmd.getName().equalsIgnoreCase("arena")) {

            if (args[0].equalsIgnoreCase("create")) {
                if (args.length != 2) return false;
                String name = args[1];
                map = new DisasterMap(main, null, null, name, null);
                p.sendMessage(ChatColor.GREEN + "Iniciada a criação do mapa " + name);
                return true;
            } else if (args[0].equalsIgnoreCase("pos1")) {
                if (args.length != 1) return false;
                Location pos = p.getLocation();
                map.setPos1(pos);
                p.sendMessage(ChatColor.GREEN + "Definida posição 1 do mapa '" + map.getName() + "' em (" + pos.getBlockX() + " " + pos.getBlockY() + " " + pos.getBlockZ() + ")");
                return true;
            } else if (args[0].equalsIgnoreCase("pos2")) {
                if (args.length != 1) return false;
                Location pos = p.getLocation();
                map.setPos2(pos);
                p.sendMessage(ChatColor.GREEN + "Definida posição 2 do mapa '" + map.getName() + "' em (" + pos.getBlockX() + " " + pos.getBlockY() + " " + pos.getBlockZ() + ")");
                return true;
            } else if (args[0].equalsIgnoreCase("spawn")) {
                if (args.length != 1) return false;
                Location pos = p.getLocation();
                if (map.getSpawns() == null)
                    map.setSpawns(new ArrayList<>());
                List<Location> locs = map.getSpawns();
                locs.add(p.getLocation());
                map.setSpawns(locs);
                p.sendMessage(ChatColor.GREEN + "Adicionado spawn do mapa '" + map.getName() + "' em (" + pos.getBlockX() + " " + pos.getBlockY() + " " + pos.getBlockZ() + ")");
                return true;
            } else if (args[0].equalsIgnoreCase("finalize")) {
                if (args.length != 1) return false;
                MapManager.getInstance().registerArena(map);
                p.sendMessage(ChatColor.GREEN + "Finalizado e registrado o mapa '" + map.getName() + "'");
                map = null;
                return true;
            }

            if (args[0].equalsIgnoreCase("tp")) {
                if (args.length != 2) return false;
                String mapname = args[1];
                if (MapManager.getInstance().getMapByName(mapname) != null) {
                    p.teleport(Objects.requireNonNull(MapManager.getInstance().getMapByName(mapname)).getSpawns().get(0));
                    p.sendMessage(ChatColor.GREEN + "Teleportado para o primeiro spawn");
                    return true;
                } else {
                    p.sendMessage(ChatColor.RED + "Não encontrada a arena '" + args[1] + "'");
                    return true;
                }
            }

            if (args[0].equalsIgnoreCase("list")) {
                if (args.length != 1) return false;
                StringBuilder message = new StringBuilder();
                message.append(ChatColor.GREEN);
                message.append("Arenas: ");
                for (DisasterMap map : MapManager.getInstance().getAllArenas()) {
                    message.append(map.getName());
                    message.append(" ");
                }
                p.sendMessage(message.toString());
                return true;
            }

            if (args[0].equalsIgnoreCase("info")) {
                if (args.length != 2) return false;
                String name = args[1];
                if (MapManager.getInstance().getMapByName(name) != null) {
                    DisasterMap map = MapManager.getInstance().getMapByName(name);
                    p.sendMessage(ChatColor.YELLOW + "Nome: " + map.getName());
                    p.sendMessage(ChatColor.YELLOW + "Pos1: " + map.getPos1().getBlockX() + " " + map.getPos1().getBlockY() + " " + map.getPos1().getBlockZ());
                    p.sendMessage(ChatColor.YELLOW + "Pos2: " + map.getPos2().getBlockX() + " " + map.getPos2().getBlockY() + " " + map.getPos2().getBlockZ());
                    p.sendMessage(ChatColor.YELLOW + "Número de spawns: " + map.getSpawns().size());
                    return true;
                } else {
                    p.sendMessage(ChatColor.RED + "Mapa não encontrado");
                    return true;
                }
            }

        }

        return false;
    }
}
