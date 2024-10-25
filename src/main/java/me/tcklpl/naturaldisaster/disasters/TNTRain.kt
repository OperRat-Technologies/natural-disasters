package me.tcklpl.naturaldisaster.disasters

import me.tcklpl.naturaldisaster.map.DisasterMap
import me.tcklpl.naturaldisaster.util.BiomeUtils
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.entity.TNTPrimed
import java.util.Objects
import java.util.Random
import java.util.concurrent.atomic.AtomicInteger

class TNTRain : Disaster("TNT Rain", true, Material.TNT, BiomeUtils.PrecipitationRequirements.ANYTHING) {
    private var r: Random? = null

    override fun setupDisaster() {
        super.setupDisaster()
        map.setPrecipitation(DisasterMap.MapPrecipitation.RANDOM)
    }

    override fun startDisaster() {
        super.startDisaster()

        val tntToSpawn = AtomicInteger(1)
        val timesRunned = AtomicInteger(0)

        val taskId = Bukkit.getServer().scheduler.scheduleSyncRepeatingTask(main, Runnable {
            for (i in 0 until tntToSpawn.get()) {
                val x = map.getLowestCoordsLocation().blockX + r!!.nextInt(map.mapSize.x)
                val z = map.getLowestCoordsLocation().blockZ + r!!.nextInt(map.mapSize.z)
                val loc = Location(
                    map.getWorld(),
                    x.toDouble(),
                    map.getHighestCoordsLocation().blockY.toDouble(),
                    z.toDouble()
                )

                val tnt = Objects.requireNonNull<World?>(map.getWorld()).spawn<TNTPrimed>(loc, TNTPrimed::class.java)

                tnt.ticksLived = 5
                tnt.fuseTicks = 20 + map.mapSize.y // 1s inicial + 1s p/ cada 20 blocos
            }
            if ((timesRunned.incrementAndGet() % 10) == 0) tntToSpawn.addAndGet(1)
        }, startDelay, 20L)

        registerTasks(taskId)
    }
}
