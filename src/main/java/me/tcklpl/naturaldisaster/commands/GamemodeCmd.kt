package me.tcklpl.naturaldisaster.commands

import org.bukkit.GameMode
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

object GamemodeCmd : CommandExecutor {

    override fun onCommand(sender: CommandSender, cmd: Command, alias: String, args: Array<out String?>?): Boolean {
        if (sender !is Player) return false;
        if (!sender.isOp) return false;
        if (cmd.name == "creative" || alias == "c")
            sender.gameMode = GameMode.CREATIVE;
        else if (cmd.name == "survival" || alias == "s")
            sender.gameMode = GameMode.SURVIVAL;
        else if (cmd.name == "adventure" || alias == "a")
            sender.gameMode = GameMode.ADVENTURE;
        else if (cmd.name == "spectator" || alias == "sp")
            sender.gameMode = GameMode.SPECTATOR;
        return true;
    }
}