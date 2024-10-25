package me.tcklpl.naturaldisaster.schematics

import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.data.BlockData
import java.lang.RuntimeException
import java.util.ArrayList
import kotlin.math.max
import kotlin.math.min

class TempSchematic(val name: String) {
    var pos1: Location? = null
    var pos2: Location? = null

    fun isFinished(): Boolean {
        return pos1 != null && pos2 != null
    }

    fun generateSchematic(): Schematic {
        if (!isFinished()) throw RuntimeException("Schematic is not yet finished")

        val minX: Int = min(pos1!!.blockX, pos2!!.blockX)
        val minY: Int = min(pos1!!.blockY, pos2!!.blockY)
        val minZ: Int = min(pos1!!.blockZ, pos2!!.blockZ)

        val maxX: Int = max(pos1!!.blockX, pos2!!.blockX)
        val maxY: Int = max(pos1!!.blockY, pos2!!.blockY)
        val maxZ: Int = max(pos1!!.blockZ, pos2!!.blockZ)

        val width = maxX - minX
        val height = maxY - minY
        val lenght = maxZ - minZ
        val world: World = checkNotNull(pos1!!.world)
        val materials = ArrayList<Material>()
        val blockData = ArrayList<BlockData>()
        for (x in minX..maxX) {
            for (y in minY..maxY) {
                for (z in minZ..maxZ) {
                    val b = world.getBlockAt(x, y, z)
                    materials.add(b.type)
                    blockData.add(b.blockData)
                }
            }
        }

        return Schematic(name, materials, blockData, width, height, lenght)
    }
}
