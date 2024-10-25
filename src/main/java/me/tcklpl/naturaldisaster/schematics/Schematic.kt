package me.tcklpl.naturaldisaster.schematics

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.block.data.BlockData
import java.io.Serializable
import java.util.ArrayList
import java.util.stream.Collectors

class Schematic(
    val name: String,
    val blocks: List<Material>,
    @Transient var blockData: List<BlockData>,
    val width: Int,
    val height: Int,
    val lenght: Int
) : Serializable {
    private val stringBlockData = ArrayList<String>()

    init {
        for (bd in blockData) this.stringBlockData.add(bd.asString)
    }

    fun buildBlockData() {
        blockData = stringBlockData.stream().map<BlockData?> { data: String -> Bukkit.createBlockData(data) }
            .collect(Collectors.toList())
    }
}
