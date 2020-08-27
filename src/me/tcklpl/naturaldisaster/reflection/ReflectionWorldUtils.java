package me.tcklpl.naturaldisaster.reflection;

import me.tcklpl.naturaldisaster.NaturalDisaster;
import me.tcklpl.naturaldisaster.reflection.exceptions.NotAllBlocksAreInTheSameChunkException;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class ReflectionWorldUtils {

    public static Object getBlockDataFromMaterial(Material m) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Object craftBlock = Objects.requireNonNull(ReflectionUtils.getBukkitClass("util.CraftMagicNumbers"))
                .getMethod("getBlock", Material.class).invoke(null, m);
        return craftBlock.getClass().getMethod("getBlockData").invoke(craftBlock);
    }

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

    public static void replaceBlocksWithMaterialAndUpdateForPlayers(List<Block> blocks, Material to, boolean applyPhysics, List<Player> players) throws NoSuchMethodException, InstantiationException, IllegalAccessException, NotAllBlocksAreInTheSameChunkException, InvocationTargetException {
        HashMap<Chunk, List<Block>> blocksPerChunk = splitBlockListPerChunk(blocks);
        for (List<Block> chunkBlocks : blocksPerChunk.values()) {
            setBlocksInSameChunk(chunkBlocks, to, applyPhysics);
        }
        for (Chunk chunk : blocksPerChunk.keySet()) {
            for (Player p : players)
                ReflectionUtils.sendPacket(p, Packets.Play.PlayOutMapChunk(chunk));
        }
    }

}
