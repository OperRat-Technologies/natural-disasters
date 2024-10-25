package me.tcklpl.naturaldisaster.disasters

import me.tcklpl.naturaldisaster.map.DisasterMap
import me.tcklpl.naturaldisaster.util.BiomeUtils
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.entity.Entity
import org.bukkit.entity.LightningStrike
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.Arrays
import java.util.concurrent.atomic.AtomicInteger
import java.util.function.Consumer
import kotlin.math.max

class Thunderstorm :
    Disaster("Thunderstorm", true, Material.CREEPER_HEAD, BiomeUtils.PrecipitationRequirements.SHOULD_RAIN) {

    private fun spawnLightningStrikeAt(loc: Location) {
        val ls = map.getWorld().spawn<LightningStrike>(loc, LightningStrike::class.java)
        val b = ls.location.block

        // break blocks around the lighting strike
        Arrays.stream<BlockFace>(BlockFace.entries.toTypedArray())
            .map { blockFace -> b.getRelative(blockFace) }
            .forEach { rel -> rel.breakNaturally(ItemStack(Material.AIR)) }
    }

    override fun setupDisaster() {
        super.setupDisaster()
        map.setPrecipitation(DisasterMap.MapPrecipitation.PRECIPITATE)
    }

    override fun startDisaster() {
        super.startDisaster()

        val randomStrikesTimesRan = AtomicInteger(0)
        val randomLightningStrikes = AtomicInteger(1)

        // Spawn random lightning strikes on the map
        val randomLightningTask = Bukkit.getScheduler().scheduleSyncRepeatingTask(main, Runnable {
            for (i in 0 until randomLightningStrikes.get()) {
                Bukkit.getScheduler().scheduleSyncDelayedTask(main, Runnable {
                    val loc = map.getRandomBlockInMap()
                    spawnLightningStrikeAt(loc)
                }, random.nextInt(3 * 20).toLong())
            }
            // Spawn 1 more lightning strike per cycle each 10s
            if (randomStrikesTimesRan.incrementAndGet() % 2 == 0) {
                randomLightningStrikes.incrementAndGet()
            }
        }, startDelay, (5 * 20).toLong())

        // Spawn lightning strikes somewhere around the players. The dispersion varies based on some conditions
        val lightningOnPlayerTask = Bukkit.getScheduler().scheduleSyncRepeatingTask(main, Runnable {
            map.playersInArena.forEach(Consumer { p: Player ->
                val y = p.getLocation().blockY
                var dispersion = 25

                // Decrease the dispersion based on the player Y level
                val heightPenalityStep = Math.floorDiv(map.mapSize.y, 10)
                val heightPenality = Math.floorDiv(y - map.minY, heightPenalityStep)
                dispersion -= heightPenality

                // Decrease the dispersion if there's no blocks above the players head
                if (y >= map.getWorld().getHighestBlockYAt(p.getLocation().blockX, p.getLocation().blockZ)) {
                    dispersion -= 5
                }

                // Decrease the dispersion in 1 for each player nearby
                val playersNearby =
                    p.getNearbyEntities(3.0, 3.0, 3.0).stream().filter { e: Entity? -> e is Player }.count()
                dispersion -= playersNearby.toInt()

                // Spawn the lightning strike somewhere in a box centered around the player based on the dispersion
                val finalDispersion = max(dispersion, 0)
                Bukkit.getScheduler().scheduleSyncDelayedTask(
                    main, Runnable {
                        spawnLightningStrikeAt(
                            map.getWorld().getHighestBlockAt(
                                p.getLocation().blockX - finalDispersion + random.nextInt(2 * finalDispersion),
                                p.getLocation().blockZ - finalDispersion + random.nextInt(2 * finalDispersion)
                            ).location
                        )
                    },
                    random.nextInt(2 * 20).toLong()
                )
            })
        }, startDelay, (3 * 20).toLong())

        registerTasks(randomLightningTask, lightningOnPlayerTask)
    }
}
