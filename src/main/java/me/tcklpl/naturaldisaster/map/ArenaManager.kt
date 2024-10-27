package me.tcklpl.naturaldisaster.map

import me.tcklpl.naturaldisaster.NaturalDisaster
import me.tcklpl.naturaldisaster.util.ActionBar
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Chunk
import org.bukkit.Difficulty
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.WorldCreator
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.io.IOException
import java.lang.NullPointerException
import java.nio.file.Files
import java.nio.file.Path
import java.util.ArrayList
import java.util.HashSet
import java.util.Objects
import java.util.concurrent.atomic.AtomicInteger
import java.util.logging.Level
import kotlin.math.abs

class ArenaManager {
    val arenas: MutableList<DisasterMap> = ArrayList<DisasterMap>()
    val main: JavaPlugin = NaturalDisaster.instance

    init {
        loadArenas()
    }

    private fun loadArenas() {
        try {
            Files.walk(Path.of(File(main.dataFolder, "arenas").path)).use { arenaFiles ->
                val count = AtomicInteger(0)
                arenaFiles.filter { path: Path -> Files.isRegularFile(path) }.forEach { config: Path ->
                    val arenaConfig: FileConfiguration = YamlConfiguration.loadConfiguration(config.toFile())
                    val name = arenaConfig.getString("name")!!
                    val worldName = arenaConfig.getString("world")!!
                    val icon = Material.valueOf(arenaConfig.getString("icon")!!)

                    val pos1x = arenaConfig.getInt("pos1.x")
                    val pos1y = arenaConfig.getInt("pos1.y")
                    val pos1z = arenaConfig.getInt("pos1.z")

                    val pos2x = arenaConfig.getInt("pos2.x")
                    val pos2y = arenaConfig.getInt("pos2.y")
                    val pos2z = arenaConfig.getInt("pos2.z")

                    val pos1 = Location(null, pos1x.toDouble(), pos1y.toDouble(), pos1z.toDouble())
                    val pos2 = Location(null, pos2x.toDouble(), pos2y.toDouble(), pos2z.toDouble())

                    val spawns: MutableList<Location> = ArrayList<Location>()
                    for (spawnCode in Objects.requireNonNull<ConfigurationSection?>(
                        arenaConfig.getConfigurationSection(
                            "spawns"
                        )
                    ).getKeys(false)) {
                        val spawnx = arenaConfig.getInt("spawns.$spawnCode.x")
                        val spawny = arenaConfig.getInt("spawns.$spawnCode.y")
                        val spawnz = arenaConfig.getInt("spawns.$spawnCode.z")
                        spawns.add(Location(null, spawnx.toDouble(), spawny.toDouble(), spawnz.toDouble()))
                    }

                    val map = DisasterMap(name, worldName, pos1, pos2, spawns, icon)
                    arenas.add(map)
                    count.getAndIncrement()
                }
                NaturalDisaster.instance.logger.info("Carregadas ${count.get()} arenas")
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun registerArena(map: DisasterMap?) {
        arenas.add(map!!)
    }

    fun saveArenas() {
        val arenaFolder = File(main.dataFolder, "arenas")
        for (map in arenas) {
            try {
                val arenaFile = File(arenaFolder, map.name + ".yml")
                if (!arenaFile.exists()) {
                    val arenaConfig: FileConfiguration = YamlConfiguration()
                    arenaConfig.set("name", map.name)
                    arenaConfig.set("world", map.worldName)
                    arenaConfig.set("icon", map.icon.toString())

                    arenaConfig.set("pos1.x", map.pos1.blockX)
                    arenaConfig.set("pos1.y", map.pos1.blockY)
                    arenaConfig.set("pos1.z", map.pos1.blockZ)

                    arenaConfig.set("pos2.x", map.pos2.blockX)
                    arenaConfig.set("pos2.y", map.pos2.blockY)
                    arenaConfig.set("pos2.z", map.pos2.blockZ)
                    3
                    var count = 0
                    for (loc in map.spawns) {
                        arenaConfig.set("spawns.spawn$count.x", loc.blockX)
                        arenaConfig.set("spawns.spawn$count.y", loc.blockY)
                        arenaConfig.set("spawns.spawn$count.z", loc.blockZ)
                        count++
                    }
                    arenaConfig.save(arenaFile)
                }
            } catch (e: NullPointerException) {
                NaturalDisaster.instance.logger.log(Level.WARNING, "Erro ao salvar arena ${map.name}")
                e.printStackTrace()
            } catch (e: IOException) {
                NaturalDisaster.instance.logger.log(Level.WARNING, "Erro ao salvar arena ${map.name}")
                e.printStackTrace()
            }
        }
    }

    fun getArenaByName(name: String?): DisasterMap {
        return arenas.stream().filter { a: DisasterMap -> a.name.equals(name, ignoreCase = true) }.findAny()
            .orElseThrow()
    }

    fun loadArenaWorld(arena: DisasterMap, callback: () -> Unit) {
        val w: World = checkNotNull(Bukkit.createWorld(WorldCreator(arena.worldName)))
        w.isAutoSave = false
        w.difficulty = Difficulty.NORMAL

        arena.updateArenaWorld(w)

        val arenaChunks: MutableSet<Chunk> = HashSet<Chunk>()

        val startChunkX = (arena.minX - 8) shr 4
        val startChunkZ = (arena.minZ - 8) shr 4

        val endChunkX = (arena.minX + arena.mapSize.x + 8) shr 4
        val endChunkZ = (arena.minZ + arena.mapSize.z + 8) shr 4

        val totalChunks: Int = abs((endChunkX - startChunkX + 1) * (endChunkZ - startChunkZ + 1))
        Bukkit.getLogger().info("Carregando $totalChunks chunks")
        ActionBar("${ChatColor.YELLOW}Carregando arena ${ChatColor.RED}${arena.name}${ChatColor.YELLOW}...").sendToAll()

        var i = 1
        for (x in startChunkX..endChunkX) {
            for (z in startChunkZ..endChunkZ) {
                val finalX = x
                val finalZ = z
                val finalI = i
                Bukkit.getScheduler().runTaskLater(main, Runnable {
                    val c = w.getChunkAt(finalX, finalZ)
                    c.load()
                    arenaChunks.add(c)
                    if (finalI == totalChunks) {
                        arena.setArenaChunks(arenaChunks)
                        callback()
                    }
                }, i.toLong())
                i++
            }
        }
    }
}
