package me.tcklpl.naturaldisaster.map;

import java.util.ArrayList;
import java.util.List;

public class MapManager {

    private static List<DisasterMap> arenas = new ArrayList<>();

    public static void registerArena(DisasterMap map) {
        if (arenas.stream().noneMatch(map::equals)) {
            arenas.add(map);
        }
    }

    public static List<DisasterMap> getAllArenas() {
        return arenas;
    }

    public static DisasterMap getMapByName(String name) {
        for (DisasterMap map : arenas)
            if (map.getName().equalsIgnoreCase(name))
                return map;
        return null;
    }

    public static DisasterMap getPlayerMap(String player) {
        for (DisasterMap map : arenas) {
            if (map.getPlayersInArena().contains(player))
                return map;
        }
        return null;
    }
}
