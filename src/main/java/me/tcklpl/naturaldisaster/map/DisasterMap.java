package me.tcklpl.naturaldisaster.map;

import me.tcklpl.naturaldisaster.NaturalDisaster;
import me.tcklpl.naturaldisaster.data.Vec3i;
import me.tcklpl.naturaldisaster.reflection.Packets;
import me.tcklpl.naturaldisaster.reflection.ReflectionUtils;
import org.bukkit.*;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class DisasterMap {

    public enum MapPrecipitation {
        PRECIPITATE, RANDOM
    }

    private final JavaPlugin main = NaturalDisaster.getMainReference();
    private final Location pos1, pos2;
    private final Location lowestCoordsLocation, highestCoordsLocation;

    private final Vec3i mapSize;
    private final String name, worldName;
    private final List<Location> spawns;
    private List<Player> playersInArena;
    private Set<Chunk> arenaChunks;
    private final Random r;
    private final int fallingBlockKillTimeSeconds = 2;
    private final Material icon;

    public DisasterMap(String name, String worldName, Location pos1, Location pos2, List<Location> spawns, Material icon) {
        this.pos1 = pos1;
        this.pos2 = pos2;
        this.name = name;
        this.worldName = worldName;
        this.spawns = spawns;
        this.icon = icon;
        playersInArena = new ArrayList<>();
        arenaChunks = new HashSet<>();
        r = new Random();

        var minX = Math.min(pos1.getBlockX(), pos2.getBlockX());
        var minY = Math.min(pos1.getBlockY(), pos2.getBlockY());
        var minZ = Math.min(pos1.getBlockZ(), pos2.getBlockZ());

        var maxX = Math.max(pos1.getBlockX(), pos2.getBlockX());
        var maxY = Math.max(pos1.getBlockY(), pos2.getBlockY());
        var maxZ = Math.max(pos1.getBlockZ(), pos2.getBlockZ());

        lowestCoordsLocation = new Location(pos1.getWorld(), minX, minY, minZ);
        highestCoordsLocation = new Location(pos1.getWorld(), maxX, maxY, maxZ);
        mapSize = new Vec3i(maxX - minX, maxY - minY, maxZ - minZ);
    }

    // Name and object comparasion

    public String getName() {
        return name;
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

    public Location getPos2() {
        return pos2;
    }

    public List<Location> getSpawns() {
        return spawns;
    }

    public World getWorld() {
        return pos1.getWorld();
    }

    public String getWorldName() {
        return worldName;
    }

    public Material getIcon() {
        return icon;
    }

    public void setArenaChunks(Set<Chunk> arenaChunks) {
        this.arenaChunks = arenaChunks;
    }

    public Location getLowestCoordsLocation() {
        return lowestCoordsLocation;
    }

    public Location getHighestCoordsLocation() {
        return highestCoordsLocation;
    }

    public Vec3i getMapSize() {
        return mapSize;
    }

    /**
     * Function to get the max Y level of all the map
     * @return the max Y level
     */
    public int getMaxYLevel() {
        List<Integer> topBlocks = new ArrayList<>();
        for (int x = lowestCoordsLocation.getBlockX(); x <= highestCoordsLocation.getBlockX(); x++) {
            for (int z = lowestCoordsLocation.getBlockZ(); z <= highestCoordsLocation.getBlockZ(); z++) {
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

        Collections.shuffle(spawns);

        lowestCoordsLocation.setWorld(w);
        highestCoordsLocation.setWorld(w);

        getWorld().setGameRule(GameRule.DO_TILE_DROPS, false);
        getWorld().setGameRule(GameRule.DO_WEATHER_CYCLE, false);
        getWorld().setGameRule(GameRule.DO_MOB_SPAWNING, false);
    }

    // Arena player management related funcions

    public List<Player> getPlayersInArena() {
        return playersInArena;
    }

    public void addAllPlayersToArena() {
        playersInArena = new ArrayList<>();
        playersInArena.addAll(Bukkit.getOnlinePlayers());
    }

    /**
     * Teleports all players currently on the playersInArena list to their respective spawns on the arena.
     */
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

        // size of the outside border that will still have the same biome as the map
        // this exists so that it doesn't make a weird weather effect as it's raining only inside the arena.
        int biomeExtraBorder = 8;

        for (int x = lowestCoordsLocation.getBlockX() - biomeExtraBorder; x <= highestCoordsLocation.getBlockX() + biomeExtraBorder; x++) {
            for (int z = lowestCoordsLocation.getBlockZ() - biomeExtraBorder; z <= highestCoordsLocation.getBlockZ() + biomeExtraBorder; z++) {
                for (int y = lowestCoordsLocation.getBlockY() - biomeExtraBorder; y <= highestCoordsLocation.getBlockY() + biomeExtraBorder; y++) {
                    Block b = getWorld().getBlockAt(x, y, z);
                    b.setBiome(biome);
                }
            }
        }

        for (Chunk c : arenaChunks) {
            var packet = Packets.Play.PlayOutMapChunk(c);
            for (Player player : getPlayersInArena()) {
                ReflectionUtils.sendPacket(player, packet);
            }
        }

    }

    /**
     * Makes the arena rain certainly or with a 50% chance.
     * @param opt if the precipitation should be certain or random.
     */
    public void setPrecipitation(MapPrecipitation opt) {
        if (opt == MapPrecipitation.PRECIPITATE || r.nextInt(2) == 0) {
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
    public void bufferedReplaceBlocks(List<Block> blocks, Material replacement, int buffer, boolean fallingBlock) {

        if (blocks.size() == 0) return;

        AtomicInteger currentBlockIndex = new AtomicInteger(0);

        BlockData replacementData = Bukkit.createBlockData(replacement);

        int bufferCycles = 1 + Math.floorDiv(blocks.size(), buffer);
        for (int currentCycle = 0; currentCycle < bufferCycles; currentCycle++) {

            Bukkit.getScheduler().scheduleSyncDelayedTask(main, () -> {
                // Previnir checagens desnecessárias
                int max = Math.min(buffer, blocks.size() - currentBlockIndex.get());
                for (int i = 0; i < max; i++) {

                    Set<FallingBlock> entities = new HashSet<>();

                    Block b = blocks.get(currentBlockIndex.get());
                    if (b.getType() != Material.AIR && fallingBlock) {
                        FallingBlock fb = getWorld().spawnFallingBlock(b.getLocation().add(0.5, 0, 0.5), b.getBlockData());
                        fb.setHurtEntities(true);
                        fb.setDropItem(false);
                        entities.add(fb);
                    }
                    b.setType(replacement, false);
                    if (b.getState() instanceof InventoryHolder)
                        b.setBlockData(replacementData);
                    if (currentBlockIndex.get() < blocks.size() - 1)
                        currentBlockIndex.incrementAndGet();

                    if (entities.size() > 0)
                        Bukkit.getScheduler().scheduleSyncDelayedTask(main, () -> {
                            entities.forEach(Entity::remove);
                        }, fallingBlockKillTimeSeconds * 20L);
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
    public void bufferedReplaceBlocks(List<Block> blocks, List<Material> replacement, int buffer, boolean fallingBlock) {

        if (blocks.size() == 0) return;

        AtomicInteger currentBlockIndex = new AtomicInteger(0);
        AtomicInteger currentRandomValue = new AtomicInteger(0);

        List<BlockData> replacementData = replacement.stream().map(Bukkit::createBlockData).toList();

        int bufferCycles = 1 + Math.floorDiv(blocks.size(), buffer);
        for (int currentCycle = 0; currentCycle < bufferCycles; currentCycle++) {

            Bukkit.getScheduler().scheduleSyncDelayedTask(main, () -> {
                // Previnir checagens desnecessárias
                int max = Math.min(buffer, blocks.size() - currentBlockIndex.get());
                for (int i = 0; i < max; i++) {

                    Set<FallingBlock> entities = new HashSet<>();

                    Block b = blocks.get(currentBlockIndex.get());
                    if (b.getType() != Material.AIR && fallingBlock) {
                        FallingBlock fb = getWorld().spawnFallingBlock(b.getLocation().add(0.5, 0, 0.5), b.getBlockData());
                        fb.setHurtEntities(true);
                        fb.setDropItem(false);
                        entities.add(fb);
                    }
                    currentRandomValue.set(r.nextInt(replacement.size()));
                    b.setType(replacement.get(currentRandomValue.get()), false);
                    if (b.getState() instanceof InventoryHolder)
                        b.setBlockData(replacementData.get(currentRandomValue.get()));

                    if (currentBlockIndex.get() < blocks.size() - 1)
                        currentBlockIndex.incrementAndGet();

                    if (entities.size() > 0)
                        Bukkit.getScheduler().scheduleSyncDelayedTask(main, () -> {
                            entities.forEach(Entity::remove);
                        }, fallingBlockKillTimeSeconds * 20L);
                }
            }, currentCycle + 1);

        }
    }

    public List<Block> bufferedExpandBlocks(List<Block> origin, int... customBuffer) {
        int buffer = customBuffer != null ? customBuffer[0] : 50;
        List<Block> response = new ArrayList<>();

        int cycles = 1 + Math.floorDiv(origin.size(), buffer);
        AtomicInteger currentBlockIndex = new AtomicInteger(0);

        for (int currentCycle = 0; currentCycle < cycles; currentCycle++) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(main, () -> {

                int max = Math.min(buffer, origin.size() - currentBlockIndex.get());

                for (int i = 0; i < max; i++) {

                    Block b = origin.get(currentBlockIndex.get());
                    Block relative;

                    relative = b.getRelative(BlockFace.NORTH);
                    if (relative.getType() != Material.AIR && !origin.contains(relative) && !response.contains(relative))
                        response.add(relative);

                    relative = b.getRelative(BlockFace.SOUTH);
                    if (relative.getType() != Material.AIR && !origin.contains(relative) && !response.contains(relative))
                        response.add(relative);

                    relative = b.getRelative(BlockFace.EAST);
                    if (relative.getType() != Material.AIR && !origin.contains(relative) && !response.contains(relative))
                        response.add(relative);

                    relative = b.getRelative(BlockFace.WEST);
                    if (relative.getType() != Material.AIR && !origin.contains(relative) && !response.contains(relative))
                        response.add(relative);

                    relative = b.getRelative(BlockFace.UP);
                    if (relative.getType() != Material.AIR && !origin.contains(relative) && !response.contains(relative))
                        response.add(relative);

                    relative = b.getRelative(BlockFace.DOWN);
                    if (relative.getType() != Material.AIR && !origin.contains(relative) && !response.contains(relative))
                        response.add(relative);

                    if (currentBlockIndex.get() < (origin.size() - 1))
                        currentBlockIndex.incrementAndGet();
                }

            }, 1 + currentCycle);
        }
        return response;
    }

    public List<Block> expandAndGetAllBlocksFromType(@NotNull Block origin, int buffer) {
        Material m = origin.getBlockData().getMaterial();
        List<Block> result = new ArrayList<>();
        List<Block> expandCandidates = new ArrayList<>();
        List<Block> currentExpansion;
        result.add(origin);
        expandCandidates.add(origin);

        do {
            currentExpansion = bufferedExpandBlocks(expandCandidates, buffer);
            expandCandidates.clear();

            for (Block b : currentExpansion) {
                if (b.getType().equals(m) && !result.contains(b) && !expandCandidates.contains(b)) {
                    expandCandidates.add(b);
                    result.add(b);
                }
            }
        } while (!expandCandidates.isEmpty());
        return result;
    }

    public void progressivelyAdvanceTime(long target, int... advancementPace) {
        // with the pace of 50/tick time will advance 1000/second
        int pace = advancementPace.length == 1 ? advancementPace[0] : 50;
        // positive diff means that we have to advance the time
        // negative diff means that we have to go back
        long diff = target - getWorld().getTime();
        if (diff != 0) {
            long steps;
            if (diff > 0) {
                steps = Math.floorDiv(diff, pace) + 1;
                for (int i = 0; i < steps; i++)
                    Bukkit.getScheduler().scheduleSyncDelayedTask(main, () -> getWorld().setTime(getWorld().getTime() + pace), i + 1L);
            } else {
                diff = Math.abs(diff);
                steps = Math.floorDiv(diff, pace) + 1;
                for (int i = 0; i < steps; i++)
                    Bukkit.getScheduler().scheduleSyncDelayedTask(main, () -> getWorld().setTime(getWorld().getTime() - pace), i + 1L);
            }
        }
    }

    public int getMinX() { return lowestCoordsLocation.getBlockX(); }

    public int getMinY() { return lowestCoordsLocation.getBlockY(); }

    public int getMinZ() { return lowestCoordsLocation.getBlockZ(); }

    public int getMaxX() { return highestCoordsLocation.getBlockX(); }

    public int getMaxY() { return highestCoordsLocation.getBlockY(); }

    public int getMaxZ() { return highestCoordsLocation.getBlockZ(); }

}
