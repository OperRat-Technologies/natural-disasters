package me.tcklpl.naturaldisaster.map;

import me.tcklpl.naturaldisaster.NaturalDisaster;
import me.tcklpl.naturaldisaster.util.ActionBar;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.stream.Stream;

public class ArenaManager {

    private final List<DisasterMap> arenas = new ArrayList<>();
    private final JavaPlugin main = NaturalDisaster.getMainReference();

    public ArenaManager() {
        loadArenas();
    }

    private void loadArenas() {
        try (Stream<Path> arenaFiles = Files.walk(Path.of(new File(main.getDataFolder(), "arenas").getPath()))) {
            AtomicInteger count = new AtomicInteger(0);
            arenaFiles.filter(Files::isRegularFile).forEach(config -> {

                FileConfiguration arenaConfig = YamlConfiguration.loadConfiguration(config.toFile());

                String name = arenaConfig.getString("name");
                String worldName = arenaConfig.getString("world");
                Material icon = Material.valueOf(arenaConfig.getString("icon"));

                int pos1x = arenaConfig.getInt("pos1.x");
                int pos1y = arenaConfig.getInt("pos1.y");
                int pos1z = arenaConfig.getInt("pos1.z");

                int pos2x = arenaConfig.getInt("pos2.x");
                int pos2y = arenaConfig.getInt("pos2.y");
                int pos2z = arenaConfig.getInt("pos2.z");

                Location pos1 = new Location(null, pos1x, pos1y, pos1z);
                Location pos2 = new Location(null, pos2x, pos2y, pos2z);

                List<Location> spawns = new ArrayList<>();
                for (String spawnCode : Objects.requireNonNull(arenaConfig.getConfigurationSection("spawns")).getKeys(false)) {
                    int spawnx = arenaConfig.getInt("spawns." + spawnCode + ".x");
                    int spawny = arenaConfig.getInt("spawns." + spawnCode + ".y");
                    int spawnz = arenaConfig.getInt("spawns." + spawnCode + ".z");
                    spawns.add(new Location(null, spawnx, spawny, spawnz));
                }

                DisasterMap map = new DisasterMap(name, worldName, pos1, pos2, spawns, icon);
                arenas.add(map);
                count.getAndIncrement();
            });
            NaturalDisaster.getMainReference().getLogger().info("Carregadas " + count.get() + " arenas");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void registerArena(DisasterMap map) {
        arenas.add(map);
    }

    public void saveArenas() {
        File arenaFolder = new File(main.getDataFolder(), "arenas");
        for (DisasterMap map : arenas) {
            try {

                File arenaFile = new File(arenaFolder, map.getName() + ".yml");
                if (!arenaFile.exists()) {
                    FileConfiguration arenaConfig = new YamlConfiguration();
                    arenaConfig.set("name", map.getName());
                    arenaConfig.set("world", map.getWorldName());
                    arenaConfig.set("icon", map.getIcon().toString());

                    arenaConfig.set("pos1.x", map.getPos1().getBlockX());
                    arenaConfig.set("pos1.y", map.getPos1().getBlockY());
                    arenaConfig.set("pos1.z", map.getPos1().getBlockZ());

                    arenaConfig.set("pos2.x", map.getPos2().getBlockX());
                    arenaConfig.set("pos2.y", map.getPos2().getBlockY());
                    arenaConfig.set("pos2.z", map.getPos2().getBlockZ());

                    int count = 0;
                    for (Location loc : map.getSpawns()) {
                        arenaConfig.set("spawns.spawn" + count + ".x", loc.getBlockX());
                        arenaConfig.set("spawns.spawn" + count + ".y", loc.getBlockY());
                        arenaConfig.set("spawns.spawn" + count + ".z", loc.getBlockZ());
                        count++;
                    }
                    arenaConfig.save(arenaFile);
                }
            } catch (NullPointerException | IOException e) {
                NaturalDisaster.getMainReference().getLogger().log(Level.WARNING, "Erro ao salvar arena " + map.getName());
                e.printStackTrace();
            }
        }
    }

    public List<DisasterMap> getArenas() {
        return arenas;
    }

    public DisasterMap getArenaByName(String name) {
        return arenas.stream().filter(a -> a.getName().equalsIgnoreCase(name)).findAny().orElseThrow();
    }

    public void loadArenaWorld(DisasterMap arena) {
        World w = Bukkit.createWorld(new WorldCreator(arena.getWorldName()));
        assert w != null;

        w.setAutoSave(false);
        w.setDifficulty(Difficulty.NORMAL);

        arena.updateArenaWorld(w);

        new ActionBar(ChatColor.GRAY + "Carregando arena...");

        Set<Chunk> arenaChunks = new HashSet<>();

        int startChunkX = (arena.minX - 8) >> 4;
        int startChunkZ = (arena.minZ - 8) >> 4;

        int endChunkX = (arena.minX + arena.gapX + 8) >> 4;
        int endChunkZ = (arena.minZ + arena.gapZ + 8) >> 4;

        int totalChunks = Math.abs((endChunkX - startChunkX + 1) * (endChunkZ - startChunkZ + 1));
        Bukkit.getLogger().info("Carregando " + totalChunks + " chunks");

        int i = 1;
        for (int x = startChunkX; x <= endChunkX; x++) {
            for (int z = startChunkZ; z <= endChunkZ; z++) {
                int finalX = x;
                int finalZ = z;
                int finalI = i;
                Bukkit.getScheduler().runTaskLater(main, () -> {
                    new ActionBar("[...] Carregando mundo: " + Math.floorDiv(finalI * 100, totalChunks) + "%").sendToAll();
                    Chunk c = w.getChunkAt(finalX, finalZ);
                    c.load();
                    arenaChunks.add(c);

                    if (finalI == totalChunks) {
                        arena.setArenaChunks(arenaChunks);
                        NaturalDisaster.getGameManager().startNextGame();
                    }
                }, i * 10L);
                i++;
            }
        }
    }
}
