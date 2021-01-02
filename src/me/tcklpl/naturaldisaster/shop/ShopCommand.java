package me.tcklpl.naturaldisaster.shop;

import me.tcklpl.naturaldisaster.GameStatus;
import me.tcklpl.naturaldisaster.NaturalDisaster;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ShopCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String alias, String[] args) {
        if (cmd.getName().equalsIgnoreCase("shop") || alias.equalsIgnoreCase("loja")) {
            if (args.length != 0) return false;
            if (sender instanceof Player) {
                Player p = (Player) sender;
                if (NaturalDisaster.getGameManager().getCurrentStatus() == GameStatus.IN_LOBBY) {
                    Shop s = new Shop(p);
                    s.show();
                } else {
                    p.sendMessage(ChatColor.RED + "Você só pode abrir a loja no lobby, caso o game já tenha acabado aguarde alguns segundos.");
                }
                return true;
            }
        }
        return false;
    }
}
