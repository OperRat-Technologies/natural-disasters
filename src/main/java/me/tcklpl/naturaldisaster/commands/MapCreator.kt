package me.tcklpl.naturaldisaster.commands

import me.tcklpl.naturaldisaster.NaturalDisaster
import me.tcklpl.naturaldisaster.map.DisasterMap
import me.tcklpl.naturaldisaster.map.TempDisasterMap
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player
import java.lang.StringBuilder
import java.util.Arrays
import java.util.HashMap
import java.util.Locale
import java.util.stream.Collectors

object MapCreator : CommandExecutor, TabCompleter {

    private val tempMaps = HashMap<Player, TempDisasterMap>()

    override fun onCommand(sender: CommandSender, cmd: Command, alias: String, args: Array<String?>): Boolean {
        if (sender !is Player) return false
        if (!sender.isOp) return false
        if (args.isEmpty()) return false

        when (args[0]!!.lowercase(Locale.getDefault())) {
            "create" -> {
                if (args.size < 2) return false
                val name = Arrays.stream<String>(args).skip(1).collect(Collectors.joining(" "))
                val worldName = sender.getLocation().world?.name!!
                if (tempMaps.containsKey(sender)) {
                    sender.sendMessage(
                        "${ChatColor.YELLOW}Você já está criando o mapa " + tempMaps.get(
                            sender
                        )!!.name + ", você pode cancelar a operação com /arena cancel"
                    )
                    return true
                }
                tempMaps.put(sender, TempDisasterMap(name, worldName))
                sender.sendMessage("${ChatColor.GREEN}Iniciada a criação do mapa $name")
                return true
            }

            "pos1" -> {
                if (args.size != 1) return false
                if (!tempMaps.containsKey(sender)) {
                    sender.sendMessage("${ChatColor.RED}Você não está criando nenhuma arena no momento.")
                    return true
                }
                val pos = sender.getLocation()
                tempMaps.get(sender)!!.pos1 = pos
                sender.sendMessage("${ChatColor.GREEN}Definida posição 1 do mapa '${tempMaps.get(sender)?.name}' em (${pos.blockX} ${pos.blockY} ${pos.blockZ})")
                return true
            }

            "pos2" -> {
                if (args.size != 1) return false
                if (!tempMaps.containsKey(sender)) {
                    sender.sendMessage("${ChatColor.RED}Você não está criando nenhuma arena no momento.")
                    return true
                }
                val pos = sender.getLocation()
                tempMaps.get(sender)!!.pos2 = pos
                sender.sendMessage("${ChatColor.GREEN}Definida posição 2 do mapa '${tempMaps.get(sender)?.name}' em (${pos.blockX} ${pos.blockY} ${pos.blockZ})")
                return true
            }

            "spawn" -> {
                if (args.size != 1) return false
                if (!tempMaps.containsKey(sender)) {
                    sender.sendMessage("${ChatColor.RED}Você não está criando nenhuma arena no momento.")
                    return true
                }
                val pos = sender.getLocation()
                val map = tempMaps.get(sender)!!
                map.addSpawn(pos)
                sender.sendMessage("${ChatColor.GREEN}Adicionado o ${map.spawns.size}º spawn do mapa '${map.name}' em (${pos.blockX} ${pos.blockY} ${pos.blockZ})")
                return true
            }

            "finalize" -> {
                if (args.size != 1) return false
                if (!tempMaps.containsKey(sender)) {
                    sender.sendMessage("${ChatColor.RED}Você não está criando nenhuma arena no momento.")
                    return true
                }
                val temp: TempDisasterMap = tempMaps.get(sender)!!
                if (!temp.isComplete()) {
                    sender.sendMessage("${ChatColor.RED}Você ainda não terminou todos os passos da criação do mapa. Você pode ver seu progresso em /arena info")
                    return true
                }
                val map = DisasterMap(temp.name, temp.worldName, temp.pos1!!, temp.pos2!!, temp.spawns, temp.icon!!)
                NaturalDisaster.instance.gameManager.arenaManager.registerArena(map)
                sender.sendMessage("${ChatColor.GREEN}Finalizado e registrado o mapa '${map.name}'")
                tempMaps.remove(sender)
                return true
            }

            "cancel" -> {
                if (args.size != 1) return false
                if (!tempMaps.containsKey(sender)) {
                    sender.sendMessage("${ChatColor.RED}Você não está criando nenhuma arena no momento.")
                    return true
                }
                sender.sendMessage("${ChatColor.GREEN}Foi cancelada a criação do mapa '${tempMaps.get(sender)!!.name}'")
                tempMaps.remove(sender)
                return true
            }

            "info" -> {
                if (args.size != 1) return false
                if (!tempMaps.containsKey(sender)) {
                    sender.sendMessage("${ChatColor.RED}Você não está criando nenhuma arena no momento.")
                    return true
                }
                val temp: TempDisasterMap = tempMaps.get(sender)!!
                sender.sendMessage(
                    "${ChatColor.YELLOW}Criação da arena '${temp.name}': " +
                            (if (temp.isComplete()) "${ChatColor.GREEN}COMPLETA" else "${ChatColor.RED}INCOMPLETA")
                )
                sender.sendMessage("${ChatColor.YELLOW}[" + (if (temp.pos1 != null) "${ChatColor.GREEN}✓" else "${ChatColor.RED}✕") + "${ChatColor.YELLOW}] Pos 1")
                sender.sendMessage("${ChatColor.YELLOW}[" + (if (temp.pos2 != null) "${ChatColor.GREEN}✓" else "${ChatColor.RED}✕") + "${ChatColor.YELLOW}] Pos 2")
                sender.sendMessage("${ChatColor.YELLOW}[" + (if (temp.spawns.size >= 24) "${ChatColor.GREEN}✓" else "${ChatColor.RED}✕") + "${ChatColor.YELLOW}] Spawns (${temp.spawns.size}/24)")
                sender.sendMessage("${ChatColor.YELLOW}[" + (if (temp.icon != null) "${ChatColor.GREEN}✓" else "${ChatColor.RED}✕") + "${ChatColor.YELLOW}] Icone")
                if (temp.isComplete()) {
                    sender.sendMessage("${ChatColor.GREEN}A criação da arena está completa, você pode finalizá-la com /arena finalize")
                }
                return true
            }

            "list" -> {
                if (args.size != 1) return false
                val message = StringBuilder()
                sender.sendMessage(ChatColor.GREEN.toString() + "Arenas:")
                for (map in NaturalDisaster.instance.gameManager.arenaManager.arenas) {
                    message.append(ChatColor.GRAY)
                    message.append(" - ")
                    message.append(map.name)
                }
                sender.sendMessage(message.toString())
                return true
            }

            "icon" -> {
                if (args.size != 1) return false
                if (!tempMaps.containsKey(sender)) {
                    sender.sendMessage(ChatColor.RED.toString() + "Você não está criando nenhuma arena no momento.")
                    return true
                }
                if (sender.inventory.itemInMainHand.type == Material.AIR) {
                    sender.sendMessage(ChatColor.RED.toString() + "Você não está segurando nenhum item no momento.")
                    return true
                }
                tempMaps.get(sender)!!.icon = sender.inventory.itemInMainHand.type
                sender.sendMessage("${ChatColor.GREEN}Ícone da arena '${tempMaps.get(sender)!!.name}' definido como '${sender.inventory.itemInMainHand.type}'")
                return true
            }

            else -> return false
        }
    }

    override fun onTabComplete(
        sender: CommandSender,
        cmd: Command,
        alias: String,
        args: Array<out String>
    ): List<String>? {
        if (args.size == 1) return listOf(
            "create",
            "pos1",
            "pos2",
            "spawn",
            "finalize",
            "cancel",
            "info",
            "list",
            "icon",
        )
        return null
    }


}
