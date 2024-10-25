package me.tcklpl.naturaldisaster.shop

import me.tcklpl.naturaldisaster.NaturalDisaster
import me.tcklpl.naturaldisaster.player.cPlayer.CPlayer
import me.tcklpl.naturaldisaster.util.ItemUtils
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player

class Shop(p: Player) {
    private val player: Player = p
    private val cPlayer: CPlayer = NaturalDisaster.instance.cPlayerManager.getCPlayer(p.uniqueId)!!

    fun show() {
        val i = Bukkit.createInventory(player, 27, "Shop")

        val money = ItemUtils.createItemStack(Material.SUNFLOWER, "${ChatColor.GREEN}$${cPlayer.money}")
        i.setItem(4, money)

        val wins = ItemUtils.createItemStack(Material.GOLD_BLOCK, "${ChatColor.GREEN}Vitórias: ${cPlayer.wins}")
        i.setItem(26, wins)

        player.openInventory(i)
    }
}
