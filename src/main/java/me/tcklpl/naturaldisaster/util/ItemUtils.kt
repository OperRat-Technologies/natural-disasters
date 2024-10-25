package me.tcklpl.naturaldisaster.util

import org.bukkit.Material
import org.bukkit.inventory.ItemStack

object ItemUtils {

    fun createItemStack(mat: Material, name: String, vararg lore: String): ItemStack {
        val item = ItemStack(mat)
        val meta = item.itemMeta!!
        meta.setDisplayName(name)
        meta.lore = lore.toMutableList()
        item.itemMeta = meta
        return item
    }
}