package me.tcklpl.naturaldisaster.disasters.tornado

import me.tcklpl.naturaldisaster.disasters.Disaster
import me.tcklpl.naturaldisaster.util.BiomeUtils
import org.bukkit.Bukkit
import org.bukkit.Material

class TornadoDisaster : Disaster("Tornado", false, Material.COBWEB, BiomeUtils.PrecipitationRequirements.ANYTHING) {

    var tornado: Tornado? = null

    override fun setupDisaster() {
        super.setupDisaster()

        val initialPosition = map.getRandomBlockInMap()
        for (y in 0..100) {
            map.getWorld().getBlockAt(initialPosition.blockX, y, initialPosition.blockZ).type = Material.RED_WOOL
        }

        tornado = Tornado(initialPosition)
    }

    override fun startDisaster() {
        super.startDisaster()

        val velocityTask = Bukkit.getScheduler().scheduleSyncRepeatingTask(main, {
            tornado?.applyVelocityForEntities(map.playersInArena)
        }, startDelay, 1L)

        registerTasks(velocityTask)
    }
}