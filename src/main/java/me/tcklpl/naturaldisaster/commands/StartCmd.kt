package me.tcklpl.naturaldisaster.commands

import me.tcklpl.naturaldisaster.NaturalDisaster
import me.tcklpl.naturaldisaster.exceptions.InvalidGameStartException
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

object StartCmd : CommandExecutor {

    override fun onCommand(sender: CommandSender, cmd: Command, alias: String, args: Array<String>): Boolean {
        if (!sender.isOp) return false
        if (!NaturalDisaster.getGameManager().isIngame) {
            sender.sendMessage("${ChatColor.GREEN}Começando o jogo...")
            try {
                NaturalDisaster.getGameManager().pickNextGame()
            } catch (e: InvalidGameStartException) {
                e.printStackTrace()
            }
        } else {
            sender.sendMessage("${ChatColor.RED}O jogo já está rolando")
        }
        return true
    }
}
