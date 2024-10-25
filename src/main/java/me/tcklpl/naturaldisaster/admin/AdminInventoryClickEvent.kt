package me.tcklpl.naturaldisaster.admin

import me.tcklpl.naturaldisaster.NaturalDisaster
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.meta.ItemMeta

object AdminInventoryClickEvent : Listener {

    private fun getClickedItemCleanName(e: InventoryClickEvent): String {
        return ChatColor.stripColor(
            java.util.Objects.requireNonNull<ItemMeta?>(
                java.util.Objects.requireNonNull<org.bukkit.inventory.ItemStack?>(
                    e.currentItem
                ).itemMeta
            ).displayName
        )!!
    }

    private fun defineMapByName(name: String, p: Player) {
        val map = NaturalDisaster.instance.gameManager.arenaManager.getArenaByName(name)
        NaturalDisaster.instance.gameManager.currentMap = map
        p.sendMessage("${ChatColor.GREEN}Mapa definido como ${ChatColor.RED}${name}")
    }

    private fun defineDisasterByName(name: String, p: Player) {
        val disaster = NaturalDisaster.instance.gameManager.disasterManager.getDisasterByName(name)
        NaturalDisaster.instance.gameManager.currentDisaster = disaster
        p.sendMessage("${ChatColor.GREEN}Desastre definido como ${ChatColor.RED}${name}")
        p.closeInventory()
    }

    @EventHandler
    fun onInvClick(e: InventoryClickEvent) {
        if (e.whoClicked !is Player) return
        if (!e.whoClicked.isOp) return
        if (e.currentItem == null) return
        if (!listOf("Admin Map Selection", "Admin Disaster Selection").contains(e.view.title)) return;

        var p = e.whoClicked as Player
        var itemName = getClickedItemCleanName(e)
        e.isCancelled = true

        when (e.view.title) {
            "Admin Map Selection" -> defineMapByName(itemName, p)
            "Admin Disaster Selection" -> defineDisasterByName(itemName, p)
            else -> p.sendMessage("${ChatColor.RED}Erro ao definir")
        }

        p.closeInventory()
    }
}
