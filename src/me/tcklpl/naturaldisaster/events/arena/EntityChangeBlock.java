package me.tcklpl.naturaldisaster.events.arena;

import me.tcklpl.naturaldisaster.GameStatus;
import me.tcklpl.naturaldisaster.NaturalDisaster;
import me.tcklpl.naturaldisaster.disasters.Volcano;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;

public class EntityChangeBlock implements Listener {

    @EventHandler
    public void onChangeBlock(EntityChangeBlockEvent e) {
        if (NaturalDisaster.getMapManager().getCurrentStatus() == GameStatus.IN_GAME) {
            if (NaturalDisaster.getMapManager().getCurrentDisaster() instanceof Volcano) {
                if (e.getTo() == Material.MAGMA_BLOCK) {
                    
                }
            }
        }
    }

}
