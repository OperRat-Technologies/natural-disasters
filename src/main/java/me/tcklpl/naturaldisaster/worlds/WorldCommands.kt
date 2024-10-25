package me.tcklpl.naturaldisaster.worlds

import me.tcklpl.naturaldisaster.NaturalDisaster
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.World
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player
import java.lang.Exception
import java.lang.StringBuilder
import java.util.Objects
import java.util.function.Consumer

object WorldCommands : CommandExecutor, TabCompleter {
    private val worldManager = NaturalDisaster.instance.worldManager

    override fun onCommand(sender: CommandSender, cmd: Command, alias: String, args: Array<String>): Boolean {
        if (!sender.isOp) return false
        if (args.isEmpty()) return false

        if (args[0].equals("create", ignoreCase = true)) {
            if (args.size != 3) return false
            if (args[1].equals("void", ignoreCase = true)) {
                val worldName = args[2]
                if (worldManager.createVoidWorld(worldName)) {
                    sender.sendMessage("${ChatColor.GREEN}Mundo criado com sucesso!")
                } else sender.sendMessage("${ChatColor.RED}Falha ao criar o mundo")
                return true
            }
        }

        if (args[0].equals("tp", ignoreCase = true)) {
            if (args.size != 2) return false
            val worldName = args[1]
            if (sender is Player) {
                if (worldManager.teleportPlayer(sender, worldName)) {
                    sender.sendMessage("${ChatColor.GREEN}Teleportado")
                } else {
                    sender.sendMessage("${ChatColor.RED}Falha ao teleportar para o mundo '$worldName'")
                }
                return true
            }
        }

        if (args[0].equals("list", ignoreCase = true)) {
            if (args.size != 1) return false
            sender.sendMessage()
            val msg = StringBuilder(ChatColor.GRAY.toString())
            NaturalDisaster.instance.worldManager.managedWorlds.forEach(Consumer { w: String? ->
                msg.append(if (Bukkit.getWorlds().stream().map<String?> { obj: World -> obj.name }
                        .anyMatch { x: String ->
                            x.equals(
                                w,
                                ignoreCase = true
                            )
                        }) ChatColor.GREEN else ChatColor.GRAY)
                msg.append(w)
                msg.append(" ")
            })
            sender.sendMessage(msg.toString())
            return true
        }

        if (args[0].equals("unload", ignoreCase = true)) {
            if (args.size != 2) return false
            val worldName = args[1]

            try {
                if (Bukkit.unloadWorld(
                        Objects.requireNonNull<World?>(Bukkit.getWorld(worldName)),
                        false
                    )
                ) sender.sendMessage("${ChatColor.GREEN}Mundo descarregado")
                else sender.sendMessage("${ChatColor.RED}Falha ao descarregar mundo")
            } catch (_: Exception) {
                sender.sendMessage("${ChatColor.RED}Falha ao descarregar mundo")
            }
            return true
        }

        return false
    }

    override fun onTabComplete(
        sender: CommandSender,
        cmd: Command,
        alias: String,
        args: Array<out String>
    ): List<String>? {

        if (args.isEmpty()) return listOf("create", "tp", "list", "unload")

        when (args[0]) {
            "create" -> {
                if (args.size == 2) return listOf("void")
            }

            "tp" -> {
                if (args.size == 2) return worldManager.managedWorlds
            }

            "unload" -> {
                if (args.size == 2) return worldManager.loadedWorlds
            }

            else -> return null
        }

        return null
    }
}
