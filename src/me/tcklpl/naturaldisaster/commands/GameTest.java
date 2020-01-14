package me.tcklpl.naturaldisaster.commands;

import me.tcklpl.naturaldisaster.map.DisasterMap;
import me.tcklpl.naturaldisaster.map.MapManager;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;
import java.util.Random;

public class GameTest implements CommandExecutor {

    JavaPlugin main;
    public GameTest(JavaPlugin main) {
        this.main = main;
    }

    private static int task;

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String alias, String[] args) {
        if (sender instanceof Player) {
            Player p = (Player) sender;
            if (cmd.getName().equalsIgnoreCase("teststart")) {
                //World world = Bukkit.createWorld(new WorldCreator("Farm"));
                World world = Bukkit.getWorld("Farm");
                assert world != null;
                world.setAutoSave(false);

                DisasterMap farm = MapManager.getMapByName("Farm");
                assert farm != null;

                for (Player pl : Bukkit.getOnlinePlayers())
                    farm.addPlayerOnArena(pl.getName());

                farm.teleportPlayersToSpawns();

                World w = farm.getPos1().getWorld();
                assert w != null : "WORLD IS NULL";

                int x1 = farm.getPos1().getBlockX();
                int x2 = farm.getPos2().getBlockX();
                int y1 = farm.getPos1().getBlockY();
                int y2 = farm.getPos2().getBlockY();
                int z1 = farm.getPos1().getBlockZ();
                int z2 = farm.getPos2().getBlockZ();

                int baseX, baseZ, top, gapX, gapZ;

                baseX = Math.min(x1, x2);
                baseZ = Math.min(z1, z2);
                top = Math.max(y1, y2);
                gapX = Math.max(x1, x2) - baseX;
                gapZ = Math.max(z1, z2) - baseZ;

                Random r = new Random();

                Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(main, () -> {
                    int x = baseX + r.nextInt(gapX);
                    int z = baseZ + r.nextInt(gapZ);
                    Location loc = new Location(Bukkit.getWorld("Farm"), x, top, z);

                    TNTPrimed tnt = Objects.requireNonNull(Bukkit.getWorld("Farm")).spawn(loc, TNTPrimed.class);

                    tnt.setTicksLived(5);
                    tnt.setFuseTicks(40); //2s

                }, 100L, 15L);
                return true;
            }
        }
        return false;
    }
}
