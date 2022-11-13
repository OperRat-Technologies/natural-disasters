package me.tcklpl.naturaldisaster.reflection;

import net.minecraft.core.Registry;
import org.bukkit.Bukkit;
import org.bukkit.block.Biome;
import org.bukkit.craftbukkit.v1_19_R1.CraftServer;

import java.util.*;

public class ReflectionWorldUtils {

    public enum Precipitation {
        ALL, RAIN, NONE, SNOW, SPECIFIC
    }

    private static final Map<net.minecraft.world.level.biome.Biome.Precipitation, List<Biome>> biomesPerPrecipitation = new HashMap<>();

    public static List<Biome> getBiomeListPerPrecipitation(Precipitation type) {

        if (biomesPerPrecipitation.isEmpty()) {
            var biomeRegistry = ((CraftServer) Bukkit.getServer()).getServer().registryHolder.registryOrThrow(Registry.BIOME_REGISTRY);

            biomeRegistry.entrySet().forEach(e -> {
                // biomeName will be like "OCEAN" or "DESERT"
                var biomeName = e.getKey().location().getPath().toUpperCase();
                // these are the same names as the bukkit biomes enum
                var bukkitBiome = Biome.valueOf(biomeName);

                if (!biomesPerPrecipitation.containsKey(e.getValue().getPrecipitation())) {
                    biomesPerPrecipitation.put(e.getValue().getPrecipitation(), new ArrayList<>());
                }

                biomesPerPrecipitation.get(e.getValue().getPrecipitation()).add(bukkitBiome);
            });
        }

        if (type == Precipitation.ALL) return Arrays.asList(Biome.values());
        if (type == Precipitation.SPECIFIC) return List.of();

        var nmsPrecipitationType = net.minecraft.world.level.biome.Biome.Precipitation.valueOf(type.toString());

        return biomesPerPrecipitation.get(nmsPrecipitationType);
    }

}
