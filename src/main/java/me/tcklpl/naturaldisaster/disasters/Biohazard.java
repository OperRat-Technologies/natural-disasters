package me.tcklpl.naturaldisaster.disasters;

import me.tcklpl.naturaldisaster.util.BiomeUtils;
import org.bukkit.Material;

public class Biohazard extends Disaster {

    public Biohazard() {
        super("Biohazard", false, Material.CHORUS_FRUIT, BiomeUtils.PrecipitationRequirements.ANYTHING);
    }

    @Override
    public void startDisaster() {
        super.startDisaster();



    }
}
