package me.tcklpl.naturaldisaster.commands

import me.tcklpl.naturaldisaster.NaturalDisaster
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

object BalanceCmd : CommandExecutor {

    override fun onCommand(sender: CommandSender, cmd: Command, alias: String, args: Array<String>): Boolean {
        if (sender !is Player) return false

        val cp = NaturalDisaster.instance.cPlayerManager.getCPlayer(sender.uniqueId)
        sender.sendMessage("${ChatColor.GOLD}Capital: $${ChatColor.BOLD}${cp?.money}")
        return true
    }
}
