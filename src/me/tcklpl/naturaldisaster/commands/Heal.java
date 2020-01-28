package me.tcklpl.naturaldisaster.commands;

import me.tcklpl.naturaldisaster.map.MapManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

public class Heal implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String alias, String[] args) {
        if (sender instanceof Player && sender.isOp()) {
            Player p = (Player) sender;
            if (cmd.getName().equalsIgnoreCase("heal")) {
                p.setHealth(20);
                p.setFoodLevel(20);
                for (PotionEffect pe : p.getActivePotionEffects())
                    p.removePotionEffect(pe.getType());
                return true;
            }
        }
        return false;
    }
}
