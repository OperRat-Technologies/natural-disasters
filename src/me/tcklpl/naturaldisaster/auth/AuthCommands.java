package me.tcklpl.naturaldisaster.auth;

import me.tcklpl.naturaldisaster.NaturalDisaster;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AuthCommands implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String alias, String[] args) {
        if (cmd.getName().equalsIgnoreCase("register")) {
            if (!(sender instanceof Player)) return true;
            if (args.length != 2) return false;

            Player p = (Player) sender;

            if (args[0].equals(args[1])) {

            } else {
                p.sendMessage(ChatColor.RED + "As senhas não coincidem");
            }

            String message = args[0];
            HashingManager.HashData hs = new HashingManager.HashData(p, HashingManager.HashingOption.HASH, message, null);
            NaturalDisaster.getAuthManager().getHashingManager().addToQueue(hs);
            return true;
        }
        return false;
    }
}
