package me.tcklpl.naturaldisaster.commands;

import me.tcklpl.naturaldisaster.player.monetaryPlayer.CustomPlayerManager;
import me.tcklpl.naturaldisaster.player.monetaryPlayer.MonetaryPlayer;
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
                MonetaryPlayer mp = CustomPlayerManager.getInstance().getMonetaryPlayer(p.getUniqueId());
                p.sendMessage(ChatColor.GOLD + "Capital: $" + ChatColor.BOLD + mp.getPlayerData().getMoney());
            }
            return true;
        }
        return false;
    }
}
