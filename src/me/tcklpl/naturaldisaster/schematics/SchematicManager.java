package me.tcklpl.naturaldisaster.schematics;

import me.tcklpl.naturaldisaster.NaturalDisaster;
import org.apache.commons.lang.NullArgumentException;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;

import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class SchematicManager {

    private final List<Schematic> schematics;
    private final File schematicFolder;

    public SchematicManager() {
        schematics = new ArrayList<>();
        schematicFolder = new File(NaturalDisaster.getMainReference().getDataFolder(), "schematics");
        if (schematicFolder.mkdirs())
            NaturalDisaster.getMainReference().getLogger().info("Criada a pasta dos schematics");
        loadSchematics();
    }

    private void loadSchematics() {
        System.out.print("Carregando schematics...");
        AtomicInteger currentSchematicIndex = new AtomicInteger(1);
        int totalSchematics = schematicFolder.listFiles().length;
        Arrays.stream(schematicFolder.listFiles()).filter(f -> !f.isDirectory() && f.getName().endsWith(".schematic")).forEach(f -> {
            System.out.print("\rCarregando schematics... [" + currentSchematicIndex + "/" + totalSchematics + "] (" + f.getName() + ") [1/2] Carregando arquivo");
            try (FileInputStream fis = new FileInputStream(f); ObjectInputStream ois = new ObjectInputStream(fis)) {
                Schematic s = (Schematic) ois.readObject();
                System.out.print("\rCarregando schematics... [" + currentSchematicIndex + "/" + totalSchematics + "] (" + f.getName() + ") [2/2] Gerando BlockData");
                s.buildBlockData();
                schematics.add(s);
            } catch (IOException | ClassNotFoundException e) {
                NaturalDisaster.getMainReference().getLogger().warning("Falha ao carregar schematic " + f.getName());
            }
            currentSchematicIndex.getAndIncrement();
        });
        System.out.print("Carregando schematics... OK");
    }

    public void saveSchematics() {
        NaturalDisaster.getMainReference().getLogger().info("Salvando schematics...");
        schematics.forEach(s -> {
            String fileName = s.getName().toLowerCase().replace(" ", "_") + ".schematic";
            File schematicFile = new File(schematicFolder, fileName);
            if (!schematicFile.exists()) {
                try (FileOutputStream fos = new FileOutputStream(schematicFile); ObjectOutputStream oos = new ObjectOutputStream(fos)) {
                    oos.writeObject(s);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public boolean isNameAvailable(String name) {
        return schematics.stream().noneMatch(x -> x.getName().equalsIgnoreCase(name));
    }

    public void registerSchematic(Schematic schematic) {
        schematics.add(schematic);
    }

    public Schematic getSchematicByName(String name) {
        return schematics.stream().filter(x -> x.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public void loadSchematicAt(Location location, Schematic schematic, boolean keepSchematicAir, SchematicLoadPosition loadPosition) {
        if (location == null) throw new NullArgumentException("Location cannot be null");
        if (schematic == null) throw new NullArgumentException("Schematic cannot be null");

        int schematicX = location.getBlockX();
        int schematicY = location.getBlockY();
        int schematicZ = location.getBlockZ();

        switch (loadPosition) {
            case FLOOR_CENTER -> {
                schematicX -= Math.floorDiv(schematic.getWidth(), 2);
                schematicZ -= Math.floorDiv(schematic.getLenght(), 2);
            }
            case TRUE_CENTER -> {
                schematicX -= Math.floorDiv(schematic.getWidth(), 2);
                schematicY -= Math.floorDiv(schematic.getHeight(), 2);
                schematicZ -= Math.floorDiv(schematic.getLenght(), 2);
            }
        }

        int currentBlockIndex = 0;
        for (int x = schematicX; x <= schematicX + schematic.getWidth(); x++) {
            for (int y = schematicY; y <= schematicY + schematic.getHeight(); y++) {
                for (int z = schematicZ; z <= schematicZ + schematic.getLenght(); z++) {
                    if (!keepSchematicAir && schematic.getBlocks().get(currentBlockIndex) == Material.AIR) {
                        currentBlockIndex++;
                        continue;
                    }
                    Block b = Objects.requireNonNull(location.getWorld()).getBlockAt(x, y, z);
                    b.setType(schematic.getBlocks().get(currentBlockIndex));
                    b.setBlockData(schematic.getBlockData().get(currentBlockIndex));
                    currentBlockIndex++;
                }
            }
        }

    }
}
