package me.tcklpl.naturaldisaster.disasters;

import me.tcklpl.naturaldisaster.map.DisasterMap;
import me.tcklpl.naturaldisaster.util.BiomeUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.LightningStrike;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class Thunderstorm extends Disaster {

    public Thunderstorm() {
        super("Thunderstorm", true, Material.CREEPER_HEAD, BiomeUtils.PrecipitationRequirements.SHOULD_RAIN);
    }

    private void spawnLightningStrikeAt(Location loc) {
        LightningStrike ls = map.getWorld().spawn(loc, LightningStrike.class);
        Block b = ls.getLocation().getBlock();

        // break blocks around the lighting strike
        Arrays.stream(BlockFace.values()).map(b::getRelative).forEach(rel -> rel.breakNaturally(new ItemStack(Material.AIR)));
    }

    @Override
    public void startDisaster() {
        super.startDisaster();
        map.setPrecipitation(DisasterMap.MapPrecipitation.PRECIPITATE);

        Random r = random;

        AtomicInteger randomStrikesTimesRan = new AtomicInteger(0);
        AtomicInteger randomLightningStrikes = new AtomicInteger(1);

        // Spawn random lightning strikes on the map
        int randomLightningTask = Bukkit.getScheduler().scheduleSyncRepeatingTask(main, () -> {

            for (var i = 0; i < randomLightningStrikes.get(); i++) {
                Bukkit.getScheduler().scheduleSyncDelayedTask(main, () -> {

                    var loc = map.getRandomBlockInMap();
                    spawnLightningStrikeAt(loc);

                }, r.nextInt(3 * 20));
            }

            // Spawn 1 more lightning strike per cycle each 10s
            if (randomStrikesTimesRan.incrementAndGet() % 2 == 0) {
                randomLightningStrikes.incrementAndGet();
            }

        }, startDelay, 5 * 20);

        // Spawn lightning strikes somewhere around the players. The dispersion varies based on some conditions
        int lightningOnPlayerTask = Bukkit.getScheduler().scheduleSyncRepeatingTask(main, () -> {

            map.getPlayersInArena().forEach(p -> {
                int y = p.getLocation().getBlockY();
                int dispersion = 25;

                // Decrease the dispersion based on the player Y level
                int heightPenalityStep = Math.floorDiv(map.getMapSize().getY(), 10);
                int heightPenality = Math.floorDiv(y - map.getMinY(), heightPenalityStep);
                dispersion -= heightPenality;

                // Decrease the dispersion if there's no blocks above the players head
                if (y >= map.getWorld().getHighestBlockYAt(p.getLocation().getBlockX(), p.getLocation().getBlockZ())) {
                    dispersion -= 5;
                }

                // Decrease the dispersion in 1 for each player nearby
                var playersNearby = p.getNearbyEntities(3, 3, 3).stream().filter(e -> e instanceof Player).count();
                dispersion -= (int) playersNearby;

                // Spawn the lightning strike somewhere in a box centered around the player based on the dispersion
                int finalDispersion = Math.max(dispersion, 0);
                Bukkit.getScheduler().scheduleSyncDelayedTask(main, () ->
                        spawnLightningStrikeAt(map.getWorld().getHighestBlockAt(
                                p.getLocation().getBlockX() - finalDispersion + r.nextInt(2 * finalDispersion),
                                p.getLocation().getBlockZ() - finalDispersion + r.nextInt(2 * finalDispersion)
                        ).getLocation())
                , r.nextInt(2 * 20));
            });

        }, startDelay, 3 * 20);

        registerTasks(randomLightningTask, lightningOnPlayerTask);

    }
}
