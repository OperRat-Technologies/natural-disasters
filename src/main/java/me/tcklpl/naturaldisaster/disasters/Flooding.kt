package me.tcklpl.naturaldisaster.disasters

import me.tcklpl.naturaldisaster.map.DisasterMap
import me.tcklpl.naturaldisaster.util.BiomeUtils
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.data.Waterlogged
import java.util.ArrayList
import java.util.HashMap
import java.util.concurrent.atomic.AtomicInteger

class Flooding : Disaster("Flooding", true, Material.WATER_BUCKET, BiomeUtils.PrecipitationRequirements.SHOULD_RAIN) {

    private val blocksToChangePerYLevel: HashMap<Int, MutableList<Block>> = HashMap<Int, MutableList<Block>>()
    private val blocksToWaterlogPerYLevel: HashMap<Int, MutableList<Block>> = HashMap<Int, MutableList<Block>>()

    private fun calcBlocksToChange() {
        for (y in map.getLowestCoordsLocation().blockY..map.getHighestCoordsLocation().blockY) {
            val change = ArrayList<Block>()
            val waterlog = ArrayList<Block>()
            for (x in map.getLowestCoordsLocation().blockX..map.getHighestCoordsLocation().blockX) {
                for (z in map.getLowestCoordsLocation().blockX..map.getHighestCoordsLocation().blockZ) {
                    val b = map.getWorld().getBlockAt(x, y, z)
                    if ((b.type == Material.AIR || !b.type.isSolid || !b.type.isOccluding) && b.blockData !is Waterlogged)
                        change.add(b)
                    if (b.blockData is Waterlogged) {
                        waterlog.add(b)
                    }
                }
            }
            blocksToChangePerYLevel.put(y, change)
            blocksToWaterlogPerYLevel.put(y, waterlog)
        }
    }

    private fun floodYLevel(y: Int) {
        map.bufferedReplaceBlocks(blocksToChangePerYLevel.get(y)!!, Material.WATER, 500, false)
        for (b in blocksToWaterlogPerYLevel.get(y)!!) {
            val waterlogged = b.blockData as Waterlogged
            waterlogged.isWaterlogged = true
            b.blockData = waterlogged
        }
    }

    override fun setupDisaster() {
        super.setupDisaster()
        blocksToChangePerYLevel.clear()
        blocksToWaterlogPerYLevel.clear()
        calcBlocksToChange()
        map.setPrecipitation(DisasterMap.MapPrecipitation.PRECIPITATE)
    }

    override fun startDisaster() {
        super.startDisaster()

        val currentY = AtomicInteger(map.getLowestCoordsLocation().blockY)

        // 5s
        val waterRiseTask = Bukkit.getScheduler().scheduleSyncRepeatingTask(main, Runnable {
            if (currentY.get() <= map.getHighestCoordsLocation().blockY) {
                floodYLevel(currentY.get())
                currentY.getAndIncrement()
            }
        }, startDelay, 5 * 20L)

        // 0.5s
        val damageTask = Bukkit.getScheduler().scheduleSyncRepeatingTask(main, Runnable {
            // Damage players that are on water
            for (p in map.playersInArena) {
                if (p.getLocation().y < currentY.get()) p.damage(1.0)
            }
        }, startDelay, 10L)

        registerTasks(waterRiseTask, damageTask)
    }
}
