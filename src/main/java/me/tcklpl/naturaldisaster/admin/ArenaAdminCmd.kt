package me.tcklpl.naturaldisaster.admin

import me.tcklpl.naturaldisaster.GameStatus
import me.tcklpl.naturaldisaster.NaturalDisaster
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.ArrayList

object ArenaAdminCmd : CommandExecutor, TabCompleter {

    private fun openMapSelectionMenu(p: Player) {
        val maps = NaturalDisaster.getGameManager().arenaManager.arenas
        val size = 9 + 9 * Math.floorDiv(maps.size - 1, 9)
        val i = Bukkit.createInventory(p, size, "Admin Map Selection")
        for (map in maps) {
            val itemStack = ItemStack(map.icon)
            val im = checkNotNull(itemStack.itemMeta)
            im.setDisplayName(map.name)
            itemStack.itemMeta = im
            i.addItem(itemStack)
        }
        p.openInventory(i)
    }

    private fun openDisasterSelectionMenu(p: Player) {
        val disasters = NaturalDisaster.getGameManager().disasterManager.disasters
        val size = 9 + 9 * Math.floorDiv(disasters.size - 1, 9)
        val i = Bukkit.createInventory(p, size, "Admin Disaster Selection")
        for (disaster in disasters) {
            val itemStack = ItemStack(disaster.getIcon())
            val im = checkNotNull(itemStack.itemMeta)
            im.setDisplayName(ChatColor.WHITE.toString() + disaster.getName())

            val lore: MutableList<String> = ArrayList<String>()
            lore.add("Playable: " + (if (disaster.isPlayable) "${ChatColor.GREEN}YES" else "${ChatColor.RED}NO"))
            im.lore = lore
            itemStack.itemMeta = im
            i.addItem(itemStack)
        }
        p.openInventory(i)
    }

    override fun onCommand(sender: CommandSender, cmd: Command, alias: String, args: Array<String>): Boolean {
        if (!sender.isOp) return false
        if (sender !is Player) return false
        if (args.size != 2) return false

        if (args[0].equals("set", ignoreCase = true)) {
            if (NaturalDisaster.getGameManager().currentStatus != GameStatus.IN_LOBBY) {
                sender.sendMessage("${ChatColor.RED}Só é possível definir mapa e desastre quando estiver em lobby, se o jogo já acabou aguarde alguns segundos.")
                return true
            }
            if (args[1].equals("map", ignoreCase = true)) {
                openMapSelectionMenu(sender)
                return true
            }
            if (args[1].equals("disaster", ignoreCase = true)) {
                openDisasterSelectionMenu(sender)
                return true
            }
        }
        return false
    }

    override fun onTabComplete(
        sender: CommandSender,
        cmd: Command,
        alias: String,
        args: Array<out String>
    ): List<String>? {

        if (args.isEmpty()) {
            return listOf("set")
        }

        if (args[0] == "set") {
            return listOf("map", "disaster")
        }

        return null
    }
}
