package me.tcklpl.naturaldisaster.worlds

import me.tcklpl.naturaldisaster.NaturalDisaster
import org.apache.commons.io.FileUtils
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.WorldCreator
import org.bukkit.entity.Player
import java.io.File
import java.io.IOException
import java.util.ArrayList
import java.util.logging.Level

class WorldManager(worlds: MutableList<String>) {
    var managedWorlds = ArrayList<String>()
    var loadedWorlds = mutableListOf("void")

    init {
        for (world in worlds) {
            val folder = File(Bukkit.getServer().worldContainer, world)
            if (!folder.exists()) {
                NaturalDisaster.instance.logger.log(Level.WARNING, "Could not load world $world, ignoring...")
            } else {
                managedWorlds.add(world)
            }
        }
        NaturalDisaster.instance.logger.info("Carregados ${managedWorlds.size} mundos")
    }

    fun createVoidWorld(name: String): Boolean {
        if (managedWorlds.stream()
                .noneMatch { anotherString: String? -> name.equals(anotherString, ignoreCase = true) }
        ) {
            val worldFolder = Bukkit.getServer().worldContainer

            val srcDir = File(worldFolder, "void-template")
            val destDir = File(worldFolder, name)
            try {
                FileUtils.copyDirectory(srcDir, destDir)
            } catch (e: IOException) {
                e.printStackTrace()
            }
            managedWorlds.add(name)
            return true
        }
        return false
    }

    fun teleportPlayer(p: Player, world: String): Boolean {
        if (managedWorlds.stream()
                .anyMatch { anotherString: String -> world.equals(anotherString, ignoreCase = true) }
        ) {
            if (loadedWorlds.stream()
                    .noneMatch { anotherString: String -> world.equals(anotherString, ignoreCase = true) }
            ) {
                Bukkit.getServer().createWorld(WorldCreator(world))
                loadedWorlds.add(world)
            }
            val loc = Location(Bukkit.getWorld(world), 0.0, 100.0, 0.0)
            p.teleport(loc)
            return true
        }
        return false
    }
}
