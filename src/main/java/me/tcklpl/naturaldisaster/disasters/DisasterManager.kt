package me.tcklpl.naturaldisaster.disasters

import java.util.ArrayList
import java.util.stream.Collectors

class DisasterManager {
    var disasters: List<Disaster> = ArrayList<Disaster>()

    init {
        loadDisasters()
    }

    private fun loadDisasters() {
        disasters = listOf(
            Blizzard(),
            Fire(),
            Flooding(),
            Thunderstorm(),
            TNTRain(),
            ToxicRain(),
        )
    }

    fun getPlayableDisasters(): MutableList<Disaster?> {
        return disasters.stream().filter { obj: Disaster? -> obj!!.playable }.collect(Collectors.toList())
    }

    fun getDisasterByName(name: String?): Disaster {
        return disasters.stream().filter { d: Disaster -> d.name.equals(name, ignoreCase = true) }.findAny()
            .orElseThrow()
    }
}
