package me.tcklpl.naturaldisaster.disasters

import me.tcklpl.naturaldisaster.map.DisasterMap
import me.tcklpl.naturaldisaster.util.BiomeUtils
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.potion.PotionEffectType
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.max

class ToxicRain : Disaster("Toxic Rain", true, Material.LIME_DYE, BiomeUtils.PrecipitationRequirements.SHOULD_RAIN) {

    override fun setupDisaster() {
        super.setupDisaster()
        map.setPrecipitation(DisasterMap.MapPrecipitation.PRECIPITATE)
    }

    override fun startDisaster() {
        super.startDisaster()

        val blocksToBreak = AtomicInteger(15)
        val timesExecuted = AtomicInteger(0)
        val currentDamage = AtomicInteger(1)
        val poisonDuration = AtomicInteger(2)
        val poisonStrenght = AtomicInteger(1)

        val corrosionTask = Bukkit.getScheduler().scheduleSyncRepeatingTask(main, Runnable {
            timesExecuted.getAndIncrement()

            // Destroy blocks
            for (i in 0 until blocksToBreak.get()) {
                val blockX = map.getLowestCoordsLocation().blockX + random.nextInt(map.mapSize.x)
                var blockY = map.getHighestCoordsLocation().blockY
                val blockZ = map.getLowestCoordsLocation().blockZ + random.nextInt(map.mapSize.z)
                while (blockY >= map.getLowestCoordsLocation().blockY) {
                    val b = map.getWorld().getBlockAt(blockX, blockY, blockZ)
                    val bUnder = map.getWorld().getBlockAt(blockX, blockY - 1, blockZ)
                    if (b.blockData.material != Material.AIR) {
                        if (bUnder.type == Material.AIR || b.blockData.material.isSolid) b.type = Material.LIME_WOOL
                        else b.type = Material.LIME_CARPET
                        Bukkit.getScheduler().scheduleSyncDelayedTask(main, Runnable { b.type = Material.AIR }, 40L)
                        break
                    }
                    blockY--
                }
            }

        }, startDelay, 20L)

        val damageTask = Bukkit.getScheduler().scheduleSyncRepeatingTask(main, Runnable {
            for (p in map.playersInArena) {
                if (p.gameMode == GameMode.ADVENTURE) {
                    val l = p.getLocation()
                    val topo = max(map.pos1.blockY, map.pos2.blockY)
                    var blockAbove = false
                    var y = l.blockY + 1
                    while (y <= topo && !blockAbove) {
                        if (map.getWorld().getBlockAt(l.blockX, y, l.blockZ).blockData.material != Material.AIR)
                            blockAbove = true
                        y++
                    }
                    if (!blockAbove) {
                        p.damage(currentDamage.get().toDouble())
                        p.addPotionEffect(
                            PotionEffectType.POISON.createEffect(
                                poisonDuration.get(),
                                poisonStrenght.get()
                            )
                        )
                    }
                }
            }
        }, startDelay, 10L)

        val increaseDifficultyTask = Bukkit.getScheduler().scheduleSyncRepeatingTask(main, Runnable {
            // Increase difficulty with time
            if ((timesExecuted.get() % 10) == 0) blocksToBreak.addAndGet(5)

            if ((timesExecuted.get() % 30) == 0) poisonDuration.addAndGet(2)

            if ((timesExecuted.get() % 60) == 0) {
                poisonStrenght.addAndGet(1)
                currentDamage.addAndGet(1)
            }
        }, startDelay, 10 * 20L)

        registerTasks(corrosionTask, damageTask, increaseDifficultyTask)
    }
}
