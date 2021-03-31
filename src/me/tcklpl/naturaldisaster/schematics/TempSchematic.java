package me.tcklpl.naturaldisaster.schematics;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;

import java.util.ArrayList;
import java.util.List;

public class TempSchematic {

    private final String name;
    private Location pos1, pos2;

    public TempSchematic(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Location getPos1() {
        return pos1;
    }

    public void setPos1(Location pos1) {
        this.pos1 = pos1;
    }

    public Location getPos2() {
        return pos2;
    }

    public void setPos2(Location pos2) {
        this.pos2 = pos2;
    }

    public boolean isFinished() {
        return pos1 != null && pos2 != null;
    }

    public Schematic generateSchematic() {
        if (!isFinished())
            throw new RuntimeException("Schematic is not yet finished");

        int minX = Math.min(pos1.getBlockX(), pos2.getBlockX());
        int minY = Math.min(pos1.getBlockY(), pos2.getBlockY());
        int minZ = Math.min(pos1.getBlockZ(), pos2.getBlockZ());

        int maxX = Math.max(pos1.getBlockX(), pos2.getBlockX());
        int maxY = Math.max(pos1.getBlockY(), pos2.getBlockY());
        int maxZ = Math.max(pos1.getBlockZ(), pos2.getBlockZ());

        int width = maxX - minX;
        int height = maxY - minY;
        int lenght = maxZ - minZ;
        World world = pos1.getWorld();
        assert world != null;

        List<Material> materials = new ArrayList<>();
        List<BlockData> blockData = new ArrayList<>();
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    Block b = world.getBlockAt(x, y, z);
                    materials.add(b.getType());
                    blockData.add(b.getBlockData());
                }
            }
        }

        return new Schematic(name, materials, blockData, width, height, lenght);
    }
}
