package me.tcklpl.naturaldisaster.events;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockFromToEvent;

public class IceMelt implements Listener {

    @EventHandler
    public void onIceMelt(BlockFadeEvent e) {
        if (e.getBlock().getType() == Material.ICE)
                e.setCancelled(true);
    }

}
