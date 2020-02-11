package me.tcklpl.naturaldisaster.commands;

import me.tcklpl.naturaldisaster.NaturalDisaster;
import me.tcklpl.naturaldisaster.player.cPlayer.CPlayer;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Balance implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String alias, String[] args) {
        if (cmd.getName().equalsIgnoreCase("balance")) {
            if (sender instanceof Player) {
                Player p = (Player) sender;
                CPlayer cp = NaturalDisaster.getPlayerManager().getCPlayer(p.getUniqueId());
                p.sendMessage(ChatColor.GOLD + "Capital: $" + ChatColor.BOLD + cp.getPlayerData().getMoney());
            }
            return true;
        }
        return false;
    }
}
