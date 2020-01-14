package me.tcklpl.naturaldisaster.commands;

import me.tcklpl.naturaldisaster.map.DisasterMap;
import me.tcklpl.naturaldisaster.map.MapManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;
import java.util.Random;

public class TntRain implements CommandExecutor {

    JavaPlugin main;
    public TntRain(JavaPlugin pl) {
        main = pl;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String alias, String[] args) {
        if (!(sender instanceof Player)) return false;
        if (cmd.getName().equalsIgnoreCase("tntrain")) {
            if (args.length != 1) return false;
            if (MapManager.getMapByName(args[0]) != null) {
                String name = args[0];
                DisasterMap map = MapManager.getMapByName(name);
                assert map != null : "MAP IS NULL";
                World w = map.getPos1().getWorld();
                assert w != null : "WORLD IS NULL";

                int x1 = map.getPos1().getBlockX();
                int x2 = map.getPos2().getBlockX();
                int y1 = map.getPos1().getBlockY();
                int y2 = map.getPos2().getBlockY();
                int z1 = map.getPos1().getBlockZ();
                int z2 = map.getPos2().getBlockZ();

                int baseX, baseZ, top, gapX, gapZ;

                baseX = Math.min(x1, x2);
                baseZ = Math.min(z1, z2);
                top = Math.max(y1, y2);
                gapX = Math.max(x1, x2) - baseX;
                gapZ = Math.max(z1, z2) - baseZ;

                final int[] counter = {0};

                Random r = new Random();

                Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(main, () -> {
                    if (counter[0] == 30)
                        Bukkit.getServer().getScheduler().cancelTasks(main);
                    int x = baseX + r.nextInt(gapX);
                    int z = baseZ + r.nextInt(gapZ);
                    Location loc = new Location(Bukkit.getWorld(name), x, top, z);

                    TNTPrimed tnt = Objects.requireNonNull(Bukkit.getWorld(name)).spawn(loc, TNTPrimed.class);

                    tnt.setTicksLived(5);
                    tnt.setFuseTicks(60);

                    counter[0]++;
                }, 20L, 20L);
                sender.sendMessage(ChatColor.RED + "CHOVE");
                return true;
            }
        }
        return false;
    }
}
