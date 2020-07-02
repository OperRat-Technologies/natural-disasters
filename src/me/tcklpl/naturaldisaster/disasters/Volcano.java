package me.tcklpl.naturaldisaster.disasters;

import me.tcklpl.naturaldisaster.map.DisasterMap;
import me.tcklpl.naturaldisaster.reflection.ReflectionUtils;
import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;

public class Volcano extends Disaster {

    public Volcano(DisasterMap map, JavaPlugin main) {
        super(map, main);
        name = "Volcano";
        hint = "Te fode lek kkk";
        playable = true;
        icon = Material.LAVA_BUCKET;
        precipitationType = ReflectionUtils.PrecipitationType.ALL;
    }

    @Override
    public void startDisaster() {
        super.startDisaster();

    }
}
