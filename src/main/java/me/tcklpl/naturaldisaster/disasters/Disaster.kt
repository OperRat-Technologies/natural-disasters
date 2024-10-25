package me.tcklpl.naturaldisaster.disasters

import me.tcklpl.naturaldisaster.NaturalDisaster
import me.tcklpl.naturaldisaster.map.DisasterMap
import me.tcklpl.naturaldisaster.util.BiomeUtils.PrecipitationRequirements
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.block.Biome
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import java.util.ArrayList
import java.util.Arrays
import java.util.Objects
import java.util.Random
import java.util.function.Consumer
import java.util.function.IntConsumer

abstract class Disaster(
    val name: String,
    val playable: Boolean,
    val icon: Material,
    val precipitationRequirements: PrecipitationRequirements
) {
    lateinit var map: DisasterMap
    var main: JavaPlugin = NaturalDisaster.instance
    private val tasks = ArrayList<Int>()
    var isActive: Boolean = false
    protected var arenaSpecificBiome: Biome? = null

    var startDelay: Long = 100L
    var timeout: Long = 3L // minutes
    var random: Random = Random()

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false
        val disaster = o as Disaster
        return name == disaster.name
    }

    override fun hashCode(): Int {
        return Objects.hash(name)
    }

    open fun setupDisaster() {
        val timeoutTaskId = Bukkit.getScheduler()
            .scheduleSyncDelayedTask(main, Runnable { this.endByTimeout() }, timeout * 20 * 60)
        val damagePlayersId = Bukkit.getScheduler()
            .scheduleSyncRepeatingTask(main, Runnable { this.damagePlayersOutsideBounds() }, 0L, 20L)
        registerTasks(timeoutTaskId, damagePlayersId)
    }

    /**
     * General method to start disaster, children inherit this method to make the disasters.
     * Method also schedules a task to time out the arena.
     */
    open fun startDisaster() {
        isActive = true
    }

    private fun damagePlayersOutsideBounds() {
        map.playersInArena.forEach(Consumer { p: Player ->
            val l = p.getLocation()
            if (l.x < map.minX || l.y < map.minY || l.z < map.minZ || l.x > map.maxX || l.y > map.maxY || l.z > map.maxZ) {
                p.damage(2.0)
            }
        })
    }

    /**
     * Private method to end by timeout, to be called by the scheduled task declared above.
     */
    private fun endByTimeout() {
        stopDisaster()
        NaturalDisaster.instance.gameManager.endByTimeout()
    }

    fun registerTasks(vararg taskNumber: Int) {
        Arrays.stream(taskNumber).forEach(IntConsumer { e: Int -> tasks.add(e) })
    }

    /**
     * Method to be called when the game ends, it cancells all the disaster tasks.
     */
    fun stopDisaster() {
        tasks.forEach(Consumer { i: Int? -> Bukkit.getScheduler().cancelTask(i!!) })
        isActive = false
    }

}
