package me.tcklpl.naturaldisaster.map

import me.tcklpl.naturaldisaster.NaturalDisaster
import me.tcklpl.naturaldisaster.data.Vec3i
import me.tcklpl.naturaldisaster.map.DisasterMap.MapPrecipitation
import me.tcklpl.naturaldisaster.reflection.Packets
import me.tcklpl.naturaldisaster.reflection.ReflectionUtils
import org.bukkit.Bukkit
import org.bukkit.Chunk
import org.bukkit.GameMode
import org.bukkit.GameRule
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.Biome
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.data.BlockData
import org.bukkit.entity.FallingBlock
import org.bukkit.entity.Player
import org.bukkit.inventory.InventoryHolder
import org.bukkit.plugin.java.JavaPlugin
import java.util.ArrayList
import java.util.HashSet
import java.util.Random
import java.util.concurrent.atomic.AtomicInteger
import java.util.function.Consumer
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class DisasterMap(
    val name: String,
    val worldName: String,
    val pos1: Location,
    val pos2: Location,
    val spawns: MutableList<Location>,
    val icon: Material
) {
    enum class MapPrecipitation {
        PRECIPITATE, RANDOM
    }

    private val main: JavaPlugin = NaturalDisaster.instance
    private val lowestCoordsLocation: Location
    private val highestCoordsLocation: Location

    val mapSize: Vec3i
    var playersInArena: MutableList<Player>
    private var arenaChunks: MutableSet<Chunk>
    private val r: Random
    private val fallingBlockKillTimeSeconds = 2

    val minX: Int = min(pos1.blockX, pos2.blockX)
    val minY: Int = min(pos1.blockY, pos2.blockY)
    val minZ: Int = min(pos1.blockZ, pos2.blockZ)

    val maxX: Int = max(pos1.blockX, pos2.blockX)
    val maxY: Int = max(pos1.blockY, pos2.blockY)
    val maxZ: Int = max(pos1.blockZ, pos2.blockZ)

    init {
        playersInArena = ArrayList<Player>()
        arenaChunks = HashSet<Chunk>()
        r = Random()

        lowestCoordsLocation = Location(pos1.world, minX.toDouble(), minY.toDouble(), minZ.toDouble())
        highestCoordsLocation = Location(pos1.world, maxX.toDouble(), maxY.toDouble(), maxZ.toDouble())
        mapSize = Vec3i(maxX - minX, maxY - minY, maxZ - minZ)
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false
        val that = o as DisasterMap
        return name == that.name
    }

    fun getWorld(): World {
        return pos1.world!!
    }

    fun setArenaChunks(arenaChunks: MutableSet<Chunk>) {
        this.arenaChunks = arenaChunks
    }

    fun getLowestCoordsLocation(): Location {
        return lowestCoordsLocation
    }

    fun getHighestCoordsLocation(): Location {
        return highestCoordsLocation
    }

    /**
     * Updates arena-relative positions with given world
     *
     * @param w the world
     */
    fun updateArenaWorld(w: World?) {
        pos1.world = w
        pos2.world = w
        for (l in spawns) l.world = w

        spawns.shuffle()

        lowestCoordsLocation.world = w
        highestCoordsLocation.world = w

        getWorld().setGameRule(GameRule.DO_TILE_DROPS, false)
        getWorld().setGameRule(GameRule.DO_WEATHER_CYCLE, false)
        getWorld().setGameRule(GameRule.DO_MOB_SPAWNING, false)
    }

    fun addAllPlayersToArena() {
        playersInArena = ArrayList<Player>()
        playersInArena.addAll(Bukkit.getOnlinePlayers())
    }

    /**
     * Teleports all players currently on the playersInArena list to their respective spawns on the arena.
     */
    fun teleportPlayersToSpawns() {
        playersInArena.shuffle()
        for (i in playersInArena.indices) {
            val p = playersInArena[i]
            val spawn = checkNotNull(spawns[i])
            checkNotNull(p)
            p.gameMode = GameMode.ADVENTURE
            NaturalDisaster.instance.logger.info("Teleportando ${p.name} para spawn $i mapa ${spawn.world!!.name} (${spawn.blockX}, ${spawn.blockY}, ${spawn.blockZ})")
            p.teleport(spawn)
        }
    }

    // Arena mechanics and disaster related funcions
    fun setArenaBiome(biome: Biome) {
        // size of the outside border that will still have the same biome as the map
        // this exists so that it doesn't make a weird weather effect as it's raining only inside the arena.

        val biomeExtraBorder = 8

        for (x in lowestCoordsLocation.blockX - biomeExtraBorder..(highestCoordsLocation.blockX + biomeExtraBorder)) {
            for (z in lowestCoordsLocation.blockZ - biomeExtraBorder..(highestCoordsLocation.blockZ + biomeExtraBorder)) {
                for (y in lowestCoordsLocation.blockY - biomeExtraBorder..(highestCoordsLocation.blockY + biomeExtraBorder)) {
                    val b = getWorld().getBlockAt(x, y, z)
                    b.biome = biome
                }
            }
        }

        for (c in arenaChunks) {
            val packet = Packets.Play.playOutMapChunk(c)
            for (player in playersInArena) {
                ReflectionUtils.sendPacket(player, packet)
            }
        }
    }

    /**
     * Makes the arena rain certainly or with a 50% chance.
     *
     * @param opt if the precipitation should be certain or random.
     */
    fun setPrecipitation(opt: MapPrecipitation?) {
        if (opt == MapPrecipitation.PRECIPITATE || r.nextInt(2) == 0) {
            getWorld().setStorm(true)
            getWorld().weatherDuration = 600 * 20
        }
    }

    /**
     * Replaces blocks in given list with replacement material in said buffer (per tick) with the possibility of
     * spawnings fallingblock entities on the replaced blocks' location.
     *
     * @param blocks       the list of blocks to be replaced.
     * @param replacement  the replacement material.
     * @param buffer       the size of the buffer to be executed per tick.
     * @param fallingBlock to create or not fallingblock entities of replaced blocks, to be used when destroying them.
     */
    fun bufferedReplaceBlocks(blocks: List<Block>, replacement: Material, buffer: Int, fallingBlock: Boolean) {
        bufferedReplaceBlocks(blocks, listOf(replacement), buffer, fallingBlock)
    }

    /**
     * Randomly replaces block in given list with the materials in the replacement list (with even percentages) using
     * said buffer (per tick) with the possibility of spawning fallingblock entities on the replaced blocks' location.
     *
     * @param blocks       the list of blocks to be replaced.
     * @param replacement  the list of replacement materials.
     * @param buffer       the size of the buffer to be executed per tick.
     * @param fallingBlock to create or not fallingblock entities of replaced blocks, to be used when destroying them.
     */
    fun bufferedReplaceBlocks(
        blocks: List<Block>,
        replacement: List<Material>,
        buffer: Int,
        fallingBlock: Boolean
    ) {
        if (blocks.isEmpty()) return

        val currentBlockIndex = AtomicInteger(0)
        val currentRandomValue = AtomicInteger(0)

        val replacementData =
            replacement.stream().map<BlockData?> { material: Material -> Bukkit.createBlockData(material) }.toList()

        val bufferCycles = 1 + Math.floorDiv(blocks.size, buffer)
        for (currentCycle in 0 until bufferCycles) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(main, Runnable {
                // Previnir checagens desnecessárias
                val max: Int = min(buffer, blocks.size - currentBlockIndex.get())
                for (i in 0 until max) {
                    val entities: MutableSet<FallingBlock?> = HashSet<FallingBlock?>()

                    val b = blocks[currentBlockIndex.get()]
                    if (b.type != Material.AIR && fallingBlock) {
                        val fb = getWorld().spawnFallingBlock(b.location.add(0.5, 0.0, 0.5), b.blockData)
                        fb.setHurtEntities(true)
                        fb.dropItem = false
                        entities.add(fb)
                    }
                    currentRandomValue.set(r.nextInt(replacement.size))
                    b.setType(replacement[currentRandomValue.get()], false)
                    if (b.state is InventoryHolder) b.blockData = replacementData[currentRandomValue.get()]

                    if (currentBlockIndex.get() < blocks.size - 1) currentBlockIndex.incrementAndGet()

                    if (!entities.isEmpty()) Bukkit.getScheduler().scheduleSyncDelayedTask(
                        main,
                        Runnable { entities.forEach(Consumer { obj: FallingBlock? -> obj!!.remove() }) },
                        fallingBlockKillTimeSeconds * 20L
                    )
                }
            }, (currentCycle + 1).toLong())
        }
    }

    fun getRandomBlockInMap(): Location {
        val x = minX + r.nextInt(mapSize.x)
        val z = minZ + r.nextInt(mapSize.z)
        return getWorld().getHighestBlockAt(x, z).location
    }

    fun bufferedExpandBlocks(origin: MutableList<Block>, vararg customBuffer: Int): MutableList<Block> {
        val buffer = if (!customBuffer.isEmpty()) customBuffer[0] else 50
        val response: MutableList<Block> = ArrayList<Block>()

        val cycles = 1 + Math.floorDiv(origin.size, buffer)
        val currentBlockIndex = AtomicInteger(0)

        for (currentCycle in 0 until cycles) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(main, Runnable {
                val max: Int = min(buffer, origin.size - currentBlockIndex.get())
                for (i in 0 until max) {
                    val b = origin[currentBlockIndex.get()]

                    var relative = b.getRelative(BlockFace.NORTH)
                    if (relative.type != Material.AIR && !origin.contains(relative) && !response.contains(relative)) response.add(
                        relative
                    )

                    relative = b.getRelative(BlockFace.SOUTH)
                    if (relative.type != Material.AIR && !origin.contains(relative) && !response.contains(relative)) response.add(
                        relative
                    )

                    relative = b.getRelative(BlockFace.EAST)
                    if (relative.type != Material.AIR && !origin.contains(relative) && !response.contains(relative)) response.add(
                        relative
                    )

                    relative = b.getRelative(BlockFace.WEST)
                    if (relative.type != Material.AIR && !origin.contains(relative) && !response.contains(relative)) response.add(
                        relative
                    )

                    relative = b.getRelative(BlockFace.UP)
                    if (relative.type != Material.AIR && !origin.contains(relative) && !response.contains(relative)) response.add(
                        relative
                    )

                    relative = b.getRelative(BlockFace.DOWN)
                    if (relative.type != Material.AIR && !origin.contains(relative) && !response.contains(relative)) response.add(
                        relative
                    )

                    if (currentBlockIndex.get() < (origin.size - 1)) currentBlockIndex.incrementAndGet()
                }
            }, (1 + currentCycle).toLong())
        }
        return response
    }

    fun expandAndGetAllBlocksFromType(origin: Block, buffer: Int): MutableList<Block?> {
        val m = origin.blockData.material
        val result: MutableList<Block?> = ArrayList<Block?>()
        val expandCandidates: MutableList<Block> = ArrayList<Block>()
        var currentExpansion: MutableList<Block>
        result.add(origin)
        expandCandidates.add(origin)

        do {
            currentExpansion = bufferedExpandBlocks(expandCandidates, buffer)
            expandCandidates.clear()

            for (b in currentExpansion) {
                if (b.type == m && !result.contains(b) && !expandCandidates.contains(b)) {
                    expandCandidates.add(b)
                    result.add(b)
                }
            }
        } while (!expandCandidates.isEmpty())
        return result
    }

    fun progressivelyAdvanceTime(target: Long, vararg advancementPace: Int) {
        // with the pace of 50/tick time will advance 1000/second
        val pace = if (advancementPace.size == 1) advancementPace[0] else 50
        // positive diff means that we have to advance the time
        // negative diff means that we have to go back
        var diff = target - getWorld().time
        if (diff != 0L) {
            var steps: Long
            if (diff > 0) {
                steps = Math.floorDiv(diff, pace) + 1
                for (i in 0 until steps) Bukkit.getScheduler().scheduleSyncDelayedTask(
                    main,
                    Runnable { getWorld().time = getWorld().time + pace },
                    i + 1L
                )
            } else {
                diff = abs(diff)
                steps = Math.floorDiv(diff, pace) + 1
                for (i in 0 until steps) Bukkit.getScheduler().scheduleSyncDelayedTask(
                    main,
                    Runnable { getWorld().time = getWorld().time - pace },
                    i + 1L
                )
            }
        }
    }

    override fun hashCode(): Int {
        var result = main.hashCode()
        result = 31 * result + pos1.hashCode()
        result = 31 * result + pos2.hashCode()
        result = 31 * result + lowestCoordsLocation.hashCode()
        result = 31 * result + highestCoordsLocation.hashCode()
        result = 31 * result + mapSize.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + worldName.hashCode()
        result = 31 * result + spawns.hashCode()
        result = 31 * result + playersInArena.hashCode()
        result = 31 * result + arenaChunks.hashCode()
        result = 31 * result + r.hashCode()
        result = 31 * result + fallingBlockKillTimeSeconds
        result = 31 * result + icon.hashCode()
        return result
    }
}
