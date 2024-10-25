package me.tcklpl.naturaldisaster.disasters

import me.tcklpl.naturaldisaster.util.BiomeUtils
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import java.util.ArrayList
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.min

class Fire : Disaster("Fire", true, Material.FIRE_CHARGE, BiomeUtils.PrecipitationRequirements.ANYTHING) {
    private val calcBuffer = 50
    private val replacementBuffer = 200
    private val burnedBlocksMaterials = listOf(Material.COAL_BLOCK)

    var currentFireSources = ArrayList<Block>()
    var previousBurnedBlocks = ArrayList<Block>()

    private fun theresBlockInY(x: Int, z: Int): Boolean {
        for (y in map.getLowestCoordsLocation().blockY + 1..map.getHighestCoordsLocation().blockY) {
            if (map.getWorld().getBlockAt(x, y, z).type != Material.AIR) return true
        }
        return false
    }

    override fun setupDisaster() {
        super.setupDisaster()

        var sourceX: Int
        var sourceY: Int
        var sourceZ: Int
        val yCandidates: MutableList<Int?> = ArrayList<Int?>()

        currentFireSources.clear()
        previousBurnedBlocks.clear()

        do {
            sourceX = map.getLowestCoordsLocation().blockX + random.nextInt(map.mapSize.x)
            sourceZ = map.getLowestCoordsLocation().blockZ + random.nextInt(map.mapSize.z)
        } while (!theresBlockInY(sourceX, sourceZ))

        for (y in map.getLowestCoordsLocation().blockY..map.getHighestCoordsLocation().blockY) {
            if (map.getWorld().getBlockAt(sourceX, y, sourceZ).type != Material.AIR) yCandidates.add(y)
        }

        sourceY = yCandidates[random.nextInt(yCandidates.size)]!!

        var sourceBlock = map.getWorld().getBlockAt(sourceX, sourceY, sourceZ)
        currentFireSources.add(sourceBlock)
    }

    override fun startDisaster() {
        super.startDisaster()

        val timesCycled = AtomicInteger(0)

        val burnTask = Bukkit.getScheduler().scheduleSyncRepeatingTask(main, Runnable {
            if (timesCycled.get() == 0) currentFireSources[0].setType(Material.COAL_BLOCK, false)
            if ((timesCycled.incrementAndGet() % 2) == 0) {
                val currentBlockIndex = AtomicInteger(0)

                val bufferedBlocks: MutableList<Block> = ArrayList<Block>()

                val cycles = 1 + Math.floorDiv(currentFireSources.size, calcBuffer)

                for (currentCycle in 0 until cycles) {
                    Bukkit.getScheduler().scheduleSyncDelayedTask(main, Runnable {
                        val max = min(calcBuffer, (currentFireSources.size - currentBlockIndex.get()))
                        for (i in 0 until max) {
                            val b = currentFireSources[currentBlockIndex.get()]

                            for (face in BlockFace.entries) {
                                val relative = b.getRelative(face)
                                if (relative.type != Material.WATER && relative.type != Material.AIR &&
                                    !previousBurnedBlocks.contains(relative) && !bufferedBlocks.contains(relative)
                                ) bufferedBlocks.add(relative)
                            }

                            if (currentBlockIndex.get() < (currentFireSources.size - 1)) currentBlockIndex.incrementAndGet()
                        }
                    }, (1 + currentCycle).toLong())
                }

                // Schedule to the end of the buffers
                Bukkit.getScheduler().scheduleSyncDelayedTask(main, Runnable {
                    for (b in bufferedBlocks) {
                        if (!previousBurnedBlocks.contains(b)) previousBurnedBlocks.add(b)
                    }
                    map.bufferedReplaceBlocks(bufferedBlocks, Material.MAGMA_BLOCK, replacementBuffer, false)

                    Bukkit.getScheduler().scheduleSyncDelayedTask(
                        main,
                        Runnable {
                            map.bufferedReplaceBlocks(
                                bufferedBlocks,
                                burnedBlocksMaterials,
                                replacementBuffer,
                                false
                            )
                        },
                        20L
                    )

                    currentFireSources.clear()
                    currentFireSources.addAll(bufferedBlocks)
                }, (2 + cycles).toLong())
            }

        }, startDelay, 20L)

        val damageTask = Bukkit.getScheduler().scheduleSyncRepeatingTask(main, Runnable {
            for (p in map.playersInArena) {
                checkNotNull(p)
                val m = p.getLocation().block.getRelative(BlockFace.DOWN).type
                if (m == Material.MAGMA_BLOCK || m == Material.COAL_BLOCK || m == Material.BLACK_TERRACOTTA) {
                    p.damage(4.0)
                    p.fireTicks = 60
                }
            }
        }, startDelay, 20L)

        registerTasks(burnTask, damageTask)
    }
}
