package me.tcklpl.naturaldisaster.disasters;

import org.reflections.Reflections;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class DisasterManager {

    private final List<Disaster> disasters = new ArrayList<>();

    public DisasterManager() {
        loadDisasters();
    }

    private void loadDisasters() {
        Reflections reflections = new Reflections("me.tcklpl.naturaldisaster.disasters");
        Set<Class<? extends Disaster>> result = reflections.getSubTypesOf(Disaster.class);
        result.forEach(d -> {
            try {
                disasters.add(d.getConstructor().newInstance());
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                e.printStackTrace();
            }
        });
    }

    public List<Disaster> getDisasters() {
        return disasters;
    }

    public List<Disaster> getPlayableDisasters() {
        return disasters.stream().filter(Disaster::isPlayable).collect(Collectors.toList());
    }

    public Disaster getDisasterByName(String name) {
        return disasters.stream().filter(d -> d.getName().equalsIgnoreCase(name)).findAny().orElseThrow();
    }
}
