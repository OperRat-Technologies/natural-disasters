package me.tcklpl.naturaldisaster.events;

import me.tcklpl.naturaldisaster.GameStatus;
import me.tcklpl.naturaldisaster.NaturalDisaster;
import me.tcklpl.naturaldisaster.map.MapManager;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.type.Stairs;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.spigotmc.event.entity.EntityDismountEvent;

public class Chair implements Listener {

    @EventHandler
    public void onSit(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        if (p.getGameMode() == GameMode.ADVENTURE)
            if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
                if (NaturalDisaster.getMapManager().getCurrentStatus() == GameStatus.IN_LOBBY) {
                    Block b = e.getClickedBlock();
                    assert b != null;
                    if (b.getType().toString().contains("STAIRS")) {
                        Location l = b.getLocation().add(0.5, 0, 0.5);
                        Location pLoc = p.getLocation();
                        if (pLoc.getBlockY() - l.getBlockY() == 0) {

                            Stairs st = (Stairs) b.getBlockData();

                            if (st.getHalf() == Bisected.Half.BOTTOM) {

                                Block top = b.getRelative(BlockFace.UP);
                                if (top.getType() == Material.AIR) {
                                    Snowball sn = p.getWorld().spawn(l, Snowball.class);
                                    sn.setInvulnerable(true);
                                    sn.setGravity(false);
                                    sn.addPassenger(p);
                                }

                            }

                        }

                    }
                }
            }
    }

    @EventHandler
    public void onStandUp(EntityDismountEvent e) {
        if (e.getDismounted() instanceof Snowball) {
            e.getDismounted().remove();
        }
    }

}
