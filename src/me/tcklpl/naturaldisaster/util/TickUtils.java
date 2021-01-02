package me.tcklpl.naturaldisaster.util;

import me.tcklpl.naturaldisaster.NaturalDisaster;
import org.apache.commons.lang.NullArgumentException;
import org.bukkit.Bukkit;

public class TickUtils {

    /**
     * Executes given Runnable splitted between ticks to minimize lag.
     * @param timesToExecute times to execute the tunnable.
     * @param tickInterval tick interval between runs.
     * @param toCall the runnable.
     */
    public static void splitRunnablePerTickInterval(int timesToExecute, int tickInterval, Runnable toCall) {
        if (timesToExecute <= 0)
            throw new IllegalArgumentException("Times to execute needs to be a non-zero positive integer");
        if (tickInterval <= 0)
            throw new IllegalArgumentException("Tick interval needs to be a non-zero positive integer");
        if (toCall == null)
            throw new NullArgumentException("Runnable cannot be null");

        for (int i = 0; i < timesToExecute; i++) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(NaturalDisaster.getMainReference(), toCall, (long) i * tickInterval);
        }
    }
}
