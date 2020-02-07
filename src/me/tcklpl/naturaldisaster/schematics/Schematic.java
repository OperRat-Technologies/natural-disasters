package me.tcklpl.naturaldisaster.schematics;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Schematic implements Serializable {

    private List<Material> blocks;
    private List<String> stringBlockData;
    private int width, lenght, height;

    public Schematic(List<Material> blocks, List<BlockData> blockData, int width, int height, int lenght) {
        this.blocks = blocks;
        this.stringBlockData = new ArrayList<>();
        for (BlockData bd : blockData)
            this.stringBlockData.add(bd.getAsString());
        this.width = width;
        this.lenght = lenght;
        this.height = height;
    }

    public List<Material> getBlocks() {
        return blocks;
    }

    public List<BlockData> getBlockData() {
        List<BlockData> bd = new ArrayList<>();
        for (String s : stringBlockData)
            bd.add(Bukkit.createBlockData(s));
        return bd;
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
