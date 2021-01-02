package me.tcklpl.naturaldisaster.reflection;

import me.tcklpl.naturaldisaster.reflection.exceptions.NotAllBlocksAreInTheSameChunkException;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class ReflectionWorldUtils {

    public enum Precipitation {
        NONE, RAIN, SNOW, ALL, SPECIFIC
    }

    /**
     * Gets NMS block data from Bukkit's Material, used on the functions below.
     * @param m the material wich you want to convert to NMS block data.
     * @return the NMS block data.
     * @throws NoSuchMethodException if it fails making the NMS Reflection calls. (Mojang changing the fucking code).
     * @throws InvocationTargetException if it fails making the NMS Reflection calls. (Mojang changing the fucking code).
     * @throws IllegalAccessException if it fails making the NMS Reflection calls. (Mojang changing the fucking code).
     */
    public static Object getBlockDataFromMaterial(Material m) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Object craftBlock = Objects.requireNonNull(ReflectionUtils.getBukkitClass("util.CraftMagicNumbers"))
                .getMethod("getBlock", Material.class).invoke(null, m);
        return craftBlock.getClass().getMethod("getBlockData").invoke(craftBlock);
    }

    /**
     * Uses Reflection to make NMS calls in order to update a list of blocks in the same chunk. NOT TO BE USED WHEN SETTING
     * A LOW AMOUNT OF BLOCKS. Due to the reflection calls, everything is resolved and called at runtime, thus blocking the
     * JVM from making any optimizations. Only use this when setting a fuckton of blocks, in every other case it's prefered
     * and faster to use DisasterMap#bufferedReplaceBlocks.
     *
     * When setting a normal amount of blocks, this function tends to indeed make the result time more stable. However it's
     * stable ~20ms higher than literally making a for and setting blocks. (Benchmark made running Earthquake on House).
     *
     * THIS METHOD DOESN'T PRODUCE ANY LIGHT UPDATES AND DOESN'T SEND ANY CHUNK UPDATES TO THE PLAYER.
     *
     * @param blocks the list of blocks to be changed (ALL NEED TO BE IN THE SAME CHUNK).
     * @param to the material to wich the previous list of blocks will be transformed.
     * @param applyPhysics to apply block physics when changing the material.
     * @throws NoSuchMethodException if it fails making the NMS Reflection calls. (Mojang changing the fucking code).
     * @throws InvocationTargetException if it fails making the NMS Reflection calls. (Mojang changing the fucking code).
     * @throws IllegalAccessException if it fails making the NMS Reflection calls. (Mojang changing the fucking code).
     * @throws InstantiationException if it fails making the NMS Reflection calls. (Mojang changing the fucking code).
     * @throws NotAllBlocksAreInTheSameChunkException when not all blocks provided in the list are in the same chunk.
     */
    public static void setBlocksInSameChunk(List<Block> blocks, Material to, boolean applyPhysics) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, InstantiationException, NotAllBlocksAreInTheSameChunkException {
        if (blocks.isEmpty())
            return;

        Chunk chunk = blocks.get(0).getChunk();
        if (blocks.stream().anyMatch(x -> !(x.getChunk().equals(chunk))))
            throw new NotAllBlocksAreInTheSameChunkException();

        World world = blocks.get(0).getWorld();
        Location firstBlockLocation = blocks.get(0).getLocation();
        Object nmsWorld = world.getClass().getMethod("getHandle").invoke(world);
        Object nmsChunk = nmsWorld.getClass().getMethod("getChunkAt", int.class, int.class)
                .invoke(nmsWorld, firstBlockLocation.getBlockX() >> 4, firstBlockLocation.getBlockZ() >> 4);

        Constructor<?> blockPosConstructor = Objects.requireNonNull(ReflectionUtils.getNMSClass("BlockPosition"))
                .getConstructor(int.class, int.class, int.class);

        for (Block b : blocks) {
            Location blockLocation = b.getLocation();
            Object blockPos = blockPosConstructor.newInstance(blockLocation.getBlockX(), blockLocation.getBlockY(), blockLocation.getBlockZ());
            Object iBlockData = getBlockDataFromMaterial(to);
            nmsChunk.getClass().getMethod("setType",
                    ReflectionUtils.getNMSClass("BlockPosition"),
                    ReflectionUtils.getNMSClass("IBlockData"),
                    boolean.class).invoke(nmsChunk, blockPos, iBlockData, applyPhysics);
        }
    }

    /**
     * Splits given list of blocks into a hashmap of Chunk and List of blocks.
     * @param blocks the list of blocks to be split.
     * @return the blocks split into each chunk in a hashmap.
     */
    public static HashMap<Chunk, List<Block>> splitBlockListPerChunk(List<Block> blocks) {
        HashMap<Chunk, List<Block>> result = new HashMap<>();
        List<Chunk> allChunks = new ArrayList<>();
        for (Block b : blocks) {
            if (!allChunks.contains(b.getChunk()))
                allChunks.add(b.getChunk());
        }
        for (Chunk c : allChunks) {
            List<Block> chunkBlocks = new ArrayList<>();
            for (Block b : blocks) {
                if (b.getChunk().equals(c))
                    chunkBlocks.add(b);
            }
            result.put(c, chunkBlocks);
        }
        return result;
    }

    public static List<Biome> getBiomeListPerPrecipitation(Precipitation type) {
        if (type == Precipitation.ALL)
            return Arrays.asList(Biome.values());
        List<Biome> response = new ArrayList<>();

        try {
            for (Object biomeBase : (Iterable<?>) Objects.requireNonNull(ReflectionUtils.getNMSClass("RegistryGeneration"))
                    .getField("WORLDGEN_BIOME").get(null)) {

                Field precipitationInnerClassField = biomeBase.getClass().getDeclaredField("j");
                precipitationInnerClassField.setAccessible(true);

                Object precipitationInnerClass = precipitationInnerClassField.get(biomeBase);
                Field precipitationType = precipitationInnerClass.getClass().getDeclaredField("b");
                precipitationType.setAccessible(true);

                if (precipitationType.get(precipitationInnerClass).toString().equalsIgnoreCase(type.toString())) {
                    response.add(Biome.valueOf(biomeBase.toString().replace("minecraft:", "").toUpperCase()));
                }

                precipitationType.setAccessible(false);
                precipitationInnerClassField.setAccessible(false);
            }
        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        }
        return response;
    }

}
