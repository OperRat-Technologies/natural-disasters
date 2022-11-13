package me.tcklpl.naturaldisaster.disasters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class DisasterManager {

    private List<Disaster> disasters = new ArrayList<>();

    public DisasterManager() {
        loadDisasters();
    }

    private void loadDisasters() {
        disasters = new ArrayList<>(Arrays.asList(
                new Biohazard(),
                new Blizzard(),
                new Earthquake(),
                new Fire(),
                new Flooding(),
                new Thunderstorm(),
                new TNTRain(),
                new ToxicRain(),
                new Volcano()
        ));
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
