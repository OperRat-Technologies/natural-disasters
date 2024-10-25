package me.tcklpl.naturaldisaster.util

import me.tcklpl.naturaldisaster.util.BiomeUtils.PrecipitationRequirements
import org.bukkit.block.Biome
import java.util.ArrayList
import kotlin.random.Random

object BiomeUtils {
    private val noPrecipitationBiomes = listOf(
        Biome.DESERT,
        Biome.SAVANNA,
        Biome.BADLANDS
    )

    private val rainBiomes = listOf(
        Biome.FOREST,
        Biome.FLOWER_FOREST,
        Biome.TAIGA,
        Biome.BIRCH_FOREST,
        Biome.DARK_FOREST,
        Biome.JUNGLE,
        Biome.BAMBOO_JUNGLE,
        Biome.PLAINS
    )

    private val snowBiomes = listOf(
        Biome.SNOWY_PLAINS,
        Biome.SNOWY_TAIGA,
        Biome.SNOWY_SLOPES,
        Biome.ICE_SPIKES,
        Biome.FROZEN_RIVER
    )

    fun randomizeBiome(requirements: PrecipitationRequirements): Biome {
        val r = Random(System.currentTimeMillis())

        val availableBiomeLists = ArrayList<List<Biome>>()
        when (requirements) {
            PrecipitationRequirements.ANYTHING -> {
                availableBiomeLists.add(noPrecipitationBiomes)
                availableBiomeLists.add(rainBiomes)
                availableBiomeLists.add(snowBiomes)
            }

            PrecipitationRequirements.SHOULD_RAIN -> availableBiomeLists.add(rainBiomes)
            PrecipitationRequirements.SHOULD_SNOW -> availableBiomeLists.add(snowBiomes)
        }
        val biomeList = availableBiomeLists[r.nextInt(availableBiomeLists.size)]
        return biomeList[r.nextInt(biomeList.size)]
    }

    enum class PrecipitationRequirements {
        ANYTHING,
        SHOULD_RAIN,
        SHOULD_SNOW
    }
}
