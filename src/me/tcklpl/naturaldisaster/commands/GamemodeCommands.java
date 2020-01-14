package me.tcklpl.naturaldisaster.commands;

import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GamemodeCommands implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String alias, String[] args) {
        if (sender instanceof Player) {
            Player p = (Player) sender;
            if (!p.isOp()) return false;
            if (cmd.getName().equalsIgnoreCase("creative") || alias.equalsIgnoreCase("c"))
                p.setGameMode(GameMode.CREATIVE);
            else if (cmd.getName().equalsIgnoreCase("survival") || alias.equalsIgnoreCase("s"))
                p.setGameMode(GameMode.SURVIVAL);
            else if (cmd.getName().equalsIgnoreCase("adventure") || alias.equalsIgnoreCase("a"))
                p.setGameMode(GameMode.ADVENTURE);
            else if (cmd.getName().equalsIgnoreCase("spectator") || alias.equalsIgnoreCase("sp"))
                p.setGameMode(GameMode.SPECTATOR);
            return true;
        }
        return false;
    }
}
