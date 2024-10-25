package me.tcklpl.naturaldisaster.disasters

import me.tcklpl.naturaldisaster.map.DisasterMap
import me.tcklpl.naturaldisaster.util.BiomeUtils
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.data.type.Snow
import org.bukkit.inventory.InventoryHolder
import org.bukkit.util.BoundingBox
import java.util.ArrayList
import java.util.HashMap
import java.util.HashSet
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.roundToInt

class Blizzard : Disaster("Blizzard", true, Material.SNOWBALL, BiomeUtils.PrecipitationRequirements.SHOULD_SNOW) {
    private var blocksToChangePerLevel = HashMap<Int, ArrayList<Block>>()
    private val weights = mutableListOf<Double?>(0.5, 0.3, 0.2, 0.1, 0.05, 0.01, 0.005, 0.001)

    override fun setupDisaster() {
        super.setupDisaster()
        blocksToChangePerLevel = HashMap<Int, ArrayList<Block>>()

        for (y in map.getHighestCoordsLocation().blockY downTo map.getLowestCoordsLocation().blockY) {
            val layerBlocks = ArrayList<Block>()

            for (x in map.getLowestCoordsLocation().blockX..map.getHighestCoordsLocation().blockX) {
                for (z in map.getLowestCoordsLocation().blockZ..map.getHighestCoordsLocation().blockZ) {
                    val b = map.getWorld().getBlockAt(x, y, z)
                    if (b.type != Material.AIR && !b.isPassable && (b.state !is InventoryHolder)) layerBlocks.add(
                        b
                    )
                }
            }
            blocksToChangePerLevel.put(y, layerBlocks)
        }

        map.setPrecipitation(DisasterMap.MapPrecipitation.PRECIPITATE)
    }

    private fun numPartialLayers(currentY: Int, maxLayers: Int, minLayers: Int): Int {
        val diff = (maxLayers - minLayers).toFloat()
        val percentage = (currentY.toFloat() - map.minY.toFloat()) / (map.mapSize.y.toFloat())
        return (minLayers + diff * percentage).roundToInt()
    }

    override fun startDisaster() {
        super.startDisaster()

        // -------------------------------------------------------------------------------------------------------------
        // Map freezing
        // -------------------------------------------------------------------------------------------------------------
        val currentY = AtomicInteger(map.maxY)

        val blockPallete = listOf(
            Material.BLUE_ICE, Material.PACKED_ICE, Material.CYAN_STAINED_GLASS, Material.BLUE_STAINED_GLASS,
            Material.LIGHT_BLUE_STAINED_GLASS
        )
        val maxPartialLayers = 8
        val minPartialLayers = 2

        val freezeTask = Bukkit.getScheduler().scheduleSyncRepeatingTask(main, Runnable {
            if (currentY.get() >= map.getLowestCoordsLocation().blockY) {
                map.bufferedReplaceBlocks(blocksToChangePerLevel.get(currentY.get())!!, blockPallete, 500, false)

                val layersBelowToPartiallyConvert = numPartialLayers(currentY.get(), maxPartialLayers, minPartialLayers)
                val extraBlocksToReplace = HashSet<Block>()
                for (i in 1..layersBelowToPartiallyConvert) {
                    val curLowerY = currentY.get() - i
                    if (curLowerY < map.minY) break
                    val layerBlocks: MutableList<Block> = blocksToChangePerLevel.get(curLowerY)!!
                    val weight: Double = weights[i - 1]!!
                    val blocksToPick = (layerBlocks.size * weight).roundToInt()

                    for (j in 0 until blocksToPick) {
                        val pickedBlock = random!!.nextInt(layerBlocks.size)
                        extraBlocksToReplace.add(layerBlocks[pickedBlock])
                    }
                }

                map.bufferedReplaceBlocks(extraBlocksToReplace.stream().toList(), blockPallete, 500, false)
                currentY.decrementAndGet()
            }
        }, startDelay, 60L)

        // -------------------------------------------------------------------------------------------------------------
        // Snow layers increasing
        // -------------------------------------------------------------------------------------------------------------
        val blocksAddSnowLayers = AtomicInteger(50)

        val snowTask = Bukkit.getScheduler().scheduleSyncRepeatingTask(main, Runnable {
            for (i in 0 until blocksAddSnowLayers.get()) {
                val randomLocation = map!!.getRandomBlockInMap()

                // if it's a solid block, get the one on top
                if (!randomLocation.block.isPassable) {
                    randomLocation.y = randomLocation.y + 1
                }

                if (!checkIfBlockCanBecomeSnowLayer(randomLocation.block)) continue

                // snow layer already? add another layer
                if (randomLocation.block.type == Material.SNOW) {
                    val snowBlock = randomLocation.block.blockData as Snow
                    val newLayers = snowBlock.layers + 1

                    // already max layers? set as snow block
                    if (newLayers >= snowBlock.maximumLayers) {
                        randomLocation.block.type = Material.SNOW_BLOCK
                    } else {
                        snowBlock.layers = newLayers
                        randomLocation.block.blockData = snowBlock
                    }
                } else {
                    randomLocation.block.type = Material.SNOW
                }
            }
        }, 50L, 10L)

        // -------------------------------------------------------------------------------------------------------------
        // Damage players
        // -------------------------------------------------------------------------------------------------------------
        val damageTask = Bukkit.getScheduler().scheduleSyncRepeatingTask(main, Runnable {
            if (!map.playersInArena.isEmpty()) {
                for (p in map.playersInArena) {
                    checkNotNull(p)

                    if (p.gameMode == GameMode.ADVENTURE) {
                        val l = p.location
                        val b = l.block
                        var finalDamage = 0

                        // Half a heart if player is away from lights
                        if (b.lightFromBlocks < 8) finalDamage += 1

                        // Half a heart if player is above ice level
                        if (b.y >= currentY.get()) {
                            finalDamage += 2
                        }

                        if (finalDamage > 0) p.freezeTicks = 100

                        p.damage(finalDamage.toDouble())
                    }
                }
            }
        }, startDelay, 20L)

        registerTasks(freezeTask, snowTask, damageTask)
    }

    private fun checkIfBlockCanBecomeSnowLayer(b: Block): Boolean {
        val blockBelow = b.getRelative(BlockFace.DOWN)

        // blocks without collision
        if (blockBelow.isPassable) return false

        // liquids
        if (blockBelow.isLiquid) return false

        // collision with more than 1 bounding box
        val voxelShape = blockBelow.collisionShape
        if (voxelShape.boundingBoxes.size != 1) return false

        // non-cube bounding box
        val boundingBox = voxelShape.boundingBoxes.toTypedArray()[0] as BoundingBox
        return boundingBox.widthX == 1.0 && boundingBox.height == 1.0 && boundingBox.widthZ == 1.0
    }
}
