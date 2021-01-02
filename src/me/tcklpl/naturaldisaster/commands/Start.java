package me.tcklpl.naturaldisaster.commands;

import me.tcklpl.naturaldisaster.NaturalDisaster;
import me.tcklpl.naturaldisaster.exceptions.InvalidGameStartException;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public class Start implements CommandExecutor {

    JavaPlugin main;
    public Start(JavaPlugin main) {
        this.main = main;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String alias, String[] args) {
        if (cmd.getName().equalsIgnoreCase("start") && sender.isOp()) {
            if (!NaturalDisaster.getGameManager().isIngame()) {
                sender.sendMessage(ChatColor.GREEN + "Começando o jogo...");
                try {
                    NaturalDisaster.getGameManager().pickNextGame();
                } catch (InvalidGameStartException e) {
                    e.printStackTrace();
                }

                //Bukkit.getScheduler().scheduleSyncDelayedTask(main, NaturalDisaster.getMapManager()::startNextGame, 60L);

            } else {
                sender.sendMessage(ChatColor.RED + "O jogo já está rolando");
            }
            return true;
        }
        return false;
    }
}
