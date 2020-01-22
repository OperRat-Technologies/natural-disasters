package me.tcklpl.naturaldisaster.schematics;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SchematicManager {

    private static SchematicManager INSTANCE;
    private SchematicManager() {}

    public static SchematicManager getInstance() {
        if (INSTANCE == null)
            INSTANCE = new SchematicManager();
        return INSTANCE;
    }

    private Location tempPos1, tempPos2;
    private String tempName;

    public boolean saveSchematic(String name, Location pos1, Location pos2) {
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
        Schematic schematic = new Schematic(materials, blockData, width, height, lenght);
        if (!new File(Bukkit.getWorldContainer() + "/schematics").exists()) {
            if (!new File(Bukkit.getWorldContainer() + "/schematics").mkdirs()) {
                Bukkit.getLogger().warning("Erro ao criar diretório schematics");
            }
        }
        File saveFile = new File(Bukkit.getWorldContainer() + "/schematics", name + ".schematic");
        if (saveFile.exists()) {
            Bukkit.getLogger().warning("Schematic " + name + " já existente, substituindo...");
            if (!saveFile.delete()) {
                Bukkit.getLogger().warning("Erro ao apagar arquivo");
                return false;
            }
        }

        FileOutputStream fileOutputStream;
        ObjectOutputStream objectOutputStream;

        try {
            fileOutputStream = new FileOutputStream(Bukkit.getWorldContainer() + "/schematics/" + name + ".schematic");
            objectOutputStream = new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(schematic);
            objectOutputStream.close();
            Bukkit.getLogger().info("Schematic " + name + " salvo com sucesso!");
            return true;
        } catch (IOException e) {
            Bukkit.getLogger().severe("ERRO AO SALVAR SCHEMATIC");
            e.printStackTrace();
            return false;
        }
    }

    public boolean loadSchematic(String name, Location origin, boolean keepAir) {

        int originX = origin.getBlockX();
        int originY = origin.getBlockY();
        int originZ = origin.getBlockZ();

        if (!new File(Bukkit.getWorldContainer() + "/schematics").exists()) {
            Bukkit.getLogger().warning("Diretório de schematics não existe.");
            return false;
        }
        File loadFile = new File(Bukkit.getWorldContainer() + "/schematics", name + ".schematic");
        if (!loadFile.exists()) {
            Bukkit.getLogger().warning("Schematic " + name + " inexistente.");
            return false;
        }

        FileInputStream fileInputStream;
        ObjectInputStream objectInputStream;
        Schematic schematic;

        try {
            fileInputStream = new FileInputStream(loadFile);
            objectInputStream = new ObjectInputStream(fileInputStream);
            schematic = (Schematic) objectInputStream.readObject();
            objectInputStream.close();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return false;
        }

        int realignedX = originX - Math.floorDiv(schematic.getWidth(), 2);
        int realignedZ = originZ - Math.floorDiv(schematic.getLenght(), 2);

        List<Material> schematicBlocks = schematic.getBlocks();
        List<BlockData> schematicBlockData = schematic.getBlockData();

        int currentBlockIndex = 0;
        for (int x = realignedX; x <= realignedX + schematic.getWidth(); x++) {
            for (int y = originY; y <= originY + schematic.getHeight(); y++) {
                for (int z = realignedZ; z <= realignedZ + schematic.getLenght(); z++) {
                    if (keepAir || schematicBlocks.get(currentBlockIndex) != Material.AIR) {
                        Block b = Objects.requireNonNull(origin.getWorld()).getBlockAt(x, y, z);
                        b.setType(schematicBlocks.get(currentBlockIndex));
                        b.setBlockData(schematicBlockData.get(currentBlockIndex++));
                    }
                }
            }
        }
        return true;
    }

    public void setTempName(String name) {
        tempName = name;
    }

    public void setTempPos1(Location tempPos1) {
        this.tempPos1 = tempPos1;
    }

    public void setTempPos2(Location tempPos2) {
        this.tempPos2 = tempPos2;
    }

    public void finalizeCreation(Player sender) {
        if (tempName == null || tempPos1 == null || tempPos2 == null) {
            sender.sendMessage(ChatColor.RED + "Um ou mais valores nulos");
            return;
        }
        if (saveSchematic(tempName, tempPos1, tempPos2)) {
            tempName = null;
            tempPos1 = null;
            tempPos2 = null;
            sender.sendMessage(ChatColor.GREEN + "Schematic salvo com sucesso!");
        } else {
            sender.sendMessage(ChatColor.RED + "Erro interno ao salvar schematic.");
        }
    }
}
