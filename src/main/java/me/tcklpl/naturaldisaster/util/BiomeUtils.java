package me.tcklpl.naturaldisaster.util;

import me.tcklpl.naturaldisaster.NaturalDisaster;
import org.bukkit.block.Biome;

import java.util.ArrayList;
import java.util.List;

public class BiomeUtils {

    public enum PrecipitationRequirements {
        ANYTHING,
        SHOULD_RAIN,
        SHOULD_SNOW
    }

    private static List<Biome> noPrecipitationBiomes = List.of(
            Biome.DESERT,
            Biome.SAVANNA,
            Biome.BADLANDS
    );

    private static List<Biome> rainBiomes = List.of(
            Biome.FOREST,
            Biome.FLOWER_FOREST,
            Biome.TAIGA,
            Biome.BIRCH_FOREST,
            Biome.DARK_FOREST,
            Biome.JUNGLE,
            Biome.BAMBOO_JUNGLE,
            Biome.PLAINS
    );

    private static List<Biome> snowBiomes = List.of(
            Biome.SNOWY_PLAINS,
            Biome.SNOWY_TAIGA,
            Biome.SNOWY_SLOPES,
            Biome.ICE_SPIKES,
            Biome.FROZEN_RIVER
    );

    public static Biome randomizeBiome(PrecipitationRequirements requirements) {
        var r = NaturalDisaster.getRandom();
        List<List<Biome>> availableBiomeLists = new ArrayList<>();
        switch (requirements) {
            case ANYTHING -> {
                availableBiomeLists.add(noPrecipitationBiomes);
                availableBiomeLists.add(rainBiomes);
                availableBiomeLists.add(snowBiomes);
            }
            case SHOULD_RAIN -> availableBiomeLists.add(rainBiomes);
            case SHOULD_SNOW -> availableBiomeLists.add(snowBiomes);
        }
        var biomeList = availableBiomeLists.get(r.nextInt(availableBiomeLists.size()));
        return biomeList.get(r.nextInt(biomeList.size()));
    }

}
