package me.tcklpl.naturaldisaster.commands

import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

object HealCmd : CommandExecutor {

    override fun onCommand(sender: CommandSender, cmd: Command, alias: String, args: Array<out String?>?): Boolean {
        if (sender !is Player) return false;
        if (!sender.isOp) return false;
        sender.health = 20.0;
        sender.foodLevel = 20;
        for (pe in sender.activePotionEffects) {
            sender.removePotionEffect(pe.type);
        }
        return true;
    }
}