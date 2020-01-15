package me.tcklpl.naturaldisaster.map;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DisasterMap {

    private Location pos1, pos2;
    private String name;
    private List<Location> spawns;
    private List<String> playersInArena;

    public DisasterMap(Location pos1, Location pos2, String name, List<Location> spawns) {
        this.pos1 = pos1;
        this.pos2 = pos2;
        this.name = name;
        this.spawns = spawns;
        playersInArena = new ArrayList<>();
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DisasterMap that = (DisasterMap) o;
        return Objects.equals(name, that.name);
    }

    public List<String> getPlayersInArena() {
        return playersInArena;
    }

    public void setPlayersInArena(List<String> playersInArena) {
        this.playersInArena = playersInArena;
    }

    public void addPlayerOnArena(String name) {
        playersInArena.add(name);
    }

    public void removePlayerOnArena(String name) {
        playersInArena.remove(name);
    }

    public void addAllPlayersToArena() {
        playersInArena = new ArrayList<>();
        for (Player p : Bukkit.getOnlinePlayers())
            playersInArena.add(p.getName());
    }

    public void teleportPlayersToSpawns() {
        for (int i = 0; i < playersInArena.size(); i++) {
            Player p = Bukkit.getPlayer(playersInArena.get(i));
            Location spawn = spawns.get(i);
            assert spawn != null;
            assert p != null;
            p.setGameMode(GameMode.ADVENTURE);
            Bukkit.getLogger().info("Teleportando " + p.getName() + " para spawn " + i + " no mapa " +
                    Objects.requireNonNull(spawn.getWorld()).getName() + " (" + spawn.getBlockX() + " " + spawn.getBlockY() + " " +
                    spawn.getBlockZ() + ")");
            p.teleport(spawn);
        }
    }
}
