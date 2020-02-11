package me.tcklpl.naturaldisaster.map;

import me.tcklpl.naturaldisaster.NaturalDisaster;
import me.tcklpl.naturaldisaster.reflection.Packets;
import me.tcklpl.naturaldisaster.reflection.ReflectionUtils;
import org.bukkit.*;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class DisasterMap {

    private JavaPlugin main;
    private Location pos1, pos2;
    private String name;
    private List<Location> spawns;
    private List<Player> playersInArena;
    private List<Chunk> arenaChunks;
    public int x1, x2, y1, y2, z1, z2, minX, minZ, gapX, gapZ, top, floor;
    private Random r;

    public DisasterMap(JavaPlugin main, Location pos1, Location pos2, String name, List<Location> spawns) {
        this.main = main;
        this.pos1 = pos1;
        this.pos2 = pos2;
        this.name = name;
        this.spawns = spawns;
        playersInArena = new ArrayList<>();
        arenaChunks = new ArrayList<>();
        r = new Random();
    }

    // Name and object comparasion

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DisasterMap that = (DisasterMap) o;
        return Objects.equals(name, that.name);
    }

    // Arena position and border related functions

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

    public List<Location> getSpawns() {
        return spawns;
    }

    public void setSpawns(List<Location> spawns) {
        this.spawns = spawns;
    }

    public World getWorld() {
        return pos1.getWorld();
    }

    /**
     * Function to get the max Y level of all the map
     * @return the max Y level
     */
    public int getMaxYLevel() {
        List<Integer> topBlocks = new ArrayList<>();
        for (int x = minX; x <= minX + gapX + 1; x++) {
            for (int z = minZ; z <= minZ + gapZ + 1; z++) {
                topBlocks.add(getWorld().getHighestBlockYAt(x, z));
            }
        }
        Collections.sort(topBlocks);
        return topBlocks.get(topBlocks.size() - 1);
    }

    /**
     * Updates arena-relative positions with given world
     * @param w the world
     */
    public void updateArenaWorld(World w) {
        pos1.setWorld(w);
        pos2.setWorld(w);
        for (Location l : spawns)
            l.setWorld(w);

        x1 = pos1.getBlockX();
        x2 = pos2.getBlockX();
        y1 = pos1.getBlockY();
        y2 = pos2.getBlockY();
        z1 = pos1.getBlockZ();
        z2 = pos2.getBlockZ();

        minX = Math.min(x1, x2);
        minZ = Math.min(z1, z2);
        top = Math.max(y1, y2);
        floor = Math.min(y1, y2);
        gapX = Math.max(x1, x2) - minX + 1;
        gapZ = Math.max(z1, z2) - minZ + 1;

        for (int x = minX; x <= minX + gapX; x++) {
            for (int z = minZ; z <= minZ + gapZ; z++) {
                Block b = getWorld().getBlockAt(x, 0, z);
                if (!arenaChunks.contains(getWorld().getChunkAt(b)))
                    arenaChunks.add(getWorld().getChunkAt(b));
            }
        }
        getWorld().setWaterAnimalSpawnLimit(0);
        getWorld().setGameRule(GameRule.DO_TILE_DROPS, false);
        getWorld().setGameRule(GameRule.DO_WEATHER_CYCLE, false);
        getWorld().setGameRule(GameRule.DO_MOB_SPAWNING, false);
    }

    // Arena player management related funcions

    public List<Player> getPlayersInArena() {
        return playersInArena;
    }

    public void setPlayersInArena(List<Player> playersInArena) {
        this.playersInArena = playersInArena;
    }

    public void addPlayerOnArena(Player p) {
        playersInArena.add(p);
    }

    public void removePlayerOnArena(Player p) {
        playersInArena.remove(p);
    }

    public void addAllPlayersToArena() {
        playersInArena = new ArrayList<>();
        playersInArena.addAll(Bukkit.getOnlinePlayers());
    }

    public void teleportPlayersToSpawns() {
        Collections.shuffle(playersInArena);
        for (int i = 0; i < playersInArena.size(); i++) {
            Player p = playersInArena.get(i);
            Location spawn = spawns.get(i);
            assert spawn != null;
            assert p != null;
            p.setGameMode(GameMode.ADVENTURE);
            NaturalDisaster.getMainReference().getLogger().info("Teleportando " + p.getName() + " para spawn " + i + " no mapa " +
                    Objects.requireNonNull(spawn.getWorld()).getName() + " (" + spawn.getBlockX() + " " + spawn.getBlockY() + " " +
                    spawn.getBlockZ() + ")");
            p.teleport(spawn);
        }
    }

    // Arena mechanics and disaster related funcions

    public void setArenaBiome(Biome biome) {

        for (int x = minX; x <= minX + gapX; x++) {
            for (int z = minZ; z <= minZ + gapZ; z++) {
                Block b = getWorld().getBlockAt(x, 0, z);
                b.setBiome(biome);
            }
        }

        for (Chunk c : arenaChunks) {
            Object packet = Packets.Play.PlayOutMapChunk(c);
            for (Player player : getPlayersInArena()) {
                ReflectionUtils.sendPacket(player, packet);
            }
        }

    }

    public void makeRain(boolean random) {

        Random r = new Random();
        if (!random || r.nextInt(2) == 0) {
            getWorld().setStorm(true);
            getWorld().setWeatherDuration(600 * 20);
        }
    }

    /**
     * Replaces blocks in given list with replacement material in said buffer (per tick) with the possibility of
     * spawnings fallingblock entities on the replaced blocks' location.
     * @param blocks the list of blocks to be replaced.
     * @param replacement the replacement material.
     * @param buffer the size of the buffer to be executed per tick.
     * @param fallingBlock to create or not fallingblock entities of replaced blocks, to be used when destroying them.
     */
    public void bufferedBreakBlocks(List<Block> blocks, Material replacement, int buffer, boolean fallingBlock) {

        if (blocks.size() == 0) return;

        AtomicInteger currentBlockIndex = new AtomicInteger(0);

        int bufferCycles = 1 + Math.floorDiv(blocks.size(), buffer);
        for (int currentCycle = 0; currentCycle < bufferCycles; currentCycle++) {

            Bukkit.getScheduler().scheduleSyncDelayedTask(main, () -> {
                // Previnir checagens desnecessárias
                int max = Math.min(buffer, blocks.size() - currentBlockIndex.get());
                for (int i = 0; i < max; i++) {
                    Block b = blocks.get(currentBlockIndex.get());
                    if (b.getType() != Material.AIR && fallingBlock) {
                        FallingBlock fb = getWorld().spawnFallingBlock(b.getLocation().add(0.5, 0, 0.5), b.getBlockData());
                        fb.setHurtEntities(true);
                        fb.setDropItem(false);
                    }
                    b.setType(replacement, false);
                    if (currentBlockIndex.get() < blocks.size() - 1)
                        currentBlockIndex.incrementAndGet();
                }
            }, currentCycle + 1);

        }
    }

    /**
     * Randomly replaces block in given list with the materials in the replacement list (with even percentages) using
     * said buffer (per tick) with the possibility of spawning fallingblock entities on the replaced blocks' location.
     * @param blocks the list of blocks to be replaced.
     * @param replacement the list of replacement materials.
     * @param buffer the size of the buffer to be executed per tick.
     * @param fallingBlock to create or not fallingblock entities of replaced blocks, to be used when destroying them.
     */
    public void bufferedBreakBlocks(List<Block> blocks, List<Material> replacement, int buffer, boolean fallingBlock) {

        if (blocks.size() == 0) return;

        AtomicInteger currentBlockIndex = new AtomicInteger(0);

        int bufferCycles = 1 + Math.floorDiv(blocks.size(), buffer);
        for (int currentCycle = 0; currentCycle < bufferCycles; currentCycle++) {

            Bukkit.getScheduler().scheduleSyncDelayedTask(main, () -> {
                // Previnir checagens desnecessárias
                int max = Math.min(buffer, blocks.size() - currentBlockIndex.get());
                for (int i = 0; i < max; i++) {
                    Block b = blocks.get(currentBlockIndex.get());
                    if (b.getType() != Material.AIR && fallingBlock) {
                        FallingBlock fb = getWorld().spawnFallingBlock(b.getLocation().add(0.5, 0, 0.5), b.getBlockData());
                        fb.setHurtEntities(true);
                        fb.setDropItem(false);
                    }
                    b.setType(replacement.get(r.nextInt(replacement.size())), false);
                    if (currentBlockIndex.get() < blocks.size() - 1)
                        currentBlockIndex.incrementAndGet();
                }
            }, currentCycle + 1);

        }
    }

    public void damagePlayerOutsideBounds(double dmg) {
        for (Player p : playersInArena) {
            Location l = p.getLocation();
            if (l.getX() < minX || l.getX() > (minX + gapX) ||
            l.getY() < floor || l.getY() > top ||
            l.getZ() < minZ || l.getZ() > (minZ + gapZ))
                p.damage(dmg);
        }
    }

}
