package me.tcklpl.naturaldisaster.schematics;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Schematic implements Serializable {

    private final String name;
    private final List<Material> blocks;
    private final List<String> stringBlockData;
    private transient List<BlockData> blockData;
    private final int width, lenght, height;

    public Schematic(String name, List<Material> blocks, List<BlockData> blockData, int width, int height, int lenght) {
        this.name = name;
        this.blocks = blocks;
        this.stringBlockData = new ArrayList<>();
        for (BlockData bd : blockData)
            this.stringBlockData.add(bd.getAsString());
        this.blockData = blockData;
        this.width = width;
        this.lenght = lenght;
        this.height = height;
    }

    public void buildBlockData() {
        blockData = stringBlockData.stream().map(Bukkit::createBlockData).collect(Collectors.toList());
    }

    public String getName() {
        return name;
    }

    public List<Material> getBlocks() {
        return blocks;
    }

    public List<BlockData> getBlockData() {
        return blockData;
    }

    public int getWidth() {
        return width;
    }

    public int getLenght() {
        return lenght;
    }

    public int getHeight() {
        return height;
    }
}
