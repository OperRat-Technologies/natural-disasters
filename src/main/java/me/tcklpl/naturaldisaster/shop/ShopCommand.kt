package me.tcklpl.naturaldisaster.shop

import me.tcklpl.naturaldisaster.GameStatus
import me.tcklpl.naturaldisaster.NaturalDisaster
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

object ShopCommand : CommandExecutor {

    override fun onCommand(sender: CommandSender, cmd: Command, alias: String, args: Array<String>): Boolean {
        if (args.isNotEmpty()) return false
        if (sender !is Player) return false

        if (NaturalDisaster.instance.gameManager.currentStatus == GameStatus.IN_LOBBY) {
            val s = Shop(sender)
            s.show()
        } else {
            sender.sendMessage("${ChatColor.RED}Você só pode abrir a loja no lobby, caso o game já tenha acabado aguarde alguns segundos.")
        }
        return true
    }
}
