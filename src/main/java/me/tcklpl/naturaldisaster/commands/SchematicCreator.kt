package me.tcklpl.naturaldisaster.commands

import me.tcklpl.naturaldisaster.NaturalDisaster
import me.tcklpl.naturaldisaster.schematics.SchematicLoadPosition
import me.tcklpl.naturaldisaster.schematics.SchematicManager
import me.tcklpl.naturaldisaster.schematics.TempSchematic
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player
import java.util.Arrays
import java.util.HashMap
import java.util.stream.Collectors

object SchematicCreator : CommandExecutor, TabCompleter {
    private val schematicMap: MutableMap<Player?, TempSchematic> = HashMap<Player?, TempSchematic>()
    private val schematicManager: SchematicManager = NaturalDisaster.instance.schematicManager

    override fun onCommand(sender: CommandSender, cmd: Command, alias: String, args: Array<String>): Boolean {
        if (sender !is Player) return false
        if (!sender.isOp) return false
        if (args.isEmpty()) return false

        when (args[0]) {
            "create" -> {
                if (args.size < 2) return false
                val name = Arrays.stream<String>(args).skip(1).collect(Collectors.joining(" "))
                if (schematicMap.containsKey(sender)) {
                    sender.sendMessage("${ChatColor.RED}Você já está criando um schematic no momento")
                    return true
                }
                if (!schematicManager.isNameAvailable(name)) {
                    sender.sendMessage("${ChatColor.RED}Já existe um schematic com esse nome")
                    return true
                }
                schematicMap.put(sender, TempSchematic(name))
                sender.sendMessage("${ChatColor.YELLOW}Iniciada a criação do schematic '${name}'")
            }

            "pos1" -> {
                if (args.size != 1) return false
                if (!schematicMap.containsKey(sender)) {
                    sender.sendMessage("${ChatColor.RED}Você não está criando nenhum schematic no momento")
                    return true
                }
                val loc = sender.getLocation().block.location
                schematicMap[sender]!!.pos1 = loc
                sender.sendMessage("${ChatColor.YELLOW}Definida posição 1 do schematic em (${loc.x} ${loc.y} ${loc.z})")
            }

            "pos2" -> {
                if (args.size != 1) return false
                if (!schematicMap.containsKey(sender)) {
                    sender.sendMessage("${ChatColor.RED}Você não está criando nenhum schematic no momento")
                    return true
                }
                val loc = sender.getLocation().block.location
                schematicMap[sender]!!.pos2 = loc
                sender.sendMessage("${ChatColor.YELLOW}Definida posição 2 do schematic em (${loc.x} ${loc.y} ${loc.z})")
            }

            "finalize" -> {
                if (args.size != 1) return false
                if (!schematicMap.containsKey(sender)) {
                    sender.sendMessage(ChatColor.RED.toString() + "Você não está criando nenhum schematic no momento")
                    return true
                }
                val tempSchematic: TempSchematic = schematicMap[sender]!!
                if (!tempSchematic.isFinished()) {
                    sender.sendMessage("${ChatColor.RED}Você ainda não definiu os 2 pontos do schematic")
                    return true
                }
                schematicManager.registerSchematic(tempSchematic.generateSchematic())
                schematicMap.remove(sender)
                sender.sendMessage("${ChatColor.GREEN}Finalizada a criação do schematic '${tempSchematic.name}'")
            }

            "load" -> {
                if (args.size < 2) return false
                val name = Arrays.stream<String?>(args).skip(1).collect(Collectors.joining(" "))
                val requested = schematicManager.getSchematicByName(name)
                if (requested == null) {
                    sender.sendMessage("${ChatColor.RED}Não foi encontrado o schematic solicitado")
                    return true
                }
                schematicManager.loadSchematicAt(
                    sender.getLocation(),
                    requested,
                    false,
                    SchematicLoadPosition.FLOOR_CENTER
                )
            }

            else -> return false
        }
        return true
    }

    override fun onTabComplete(
        sender: CommandSender,
        cmd: Command,
        alias: String,
        args: Array<out String>
    ): List<String>? {

        if (args.isEmpty()) return listOf("create", "pos1", "pos2", "finalize", "load")

        return null
    }
}
