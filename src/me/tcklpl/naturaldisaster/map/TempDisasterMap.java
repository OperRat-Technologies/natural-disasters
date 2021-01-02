package me.tcklpl.naturaldisaster.map;

import org.bukkit.Location;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TempDisasterMap {

    private String name, worldName;
    private Location pos1, pos2;
    private final List<Location> spawns;
    private Material icon;

    public TempDisasterMap(String name, String worldName) {
        this.name = name;
        this.worldName = worldName;
        spawns = new ArrayList<>();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TempDisasterMap that = (TempDisasterMap) o;
        return Objects.equals(name, that.name) && Objects.equals(worldName, that.worldName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, worldName);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getWorldName() {
        return worldName;
    }

    public void setWorldName(String worldName) {
        this.worldName = worldName;
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

    public List<Location> getSpawns() {
        return spawns;
    }

    public void addSpawn(Location spawn) {
        this.spawns.add(spawn);
    }

    public Material getIcon() {
        return icon;
    }

    public void setIcon(Material icon) {
        this.icon = icon;
    }

    public boolean isComplete() {
        return name != null && worldName != null && pos1 != null && pos2 != null && spawns.size() >= 24 && icon != null;
    }
}
