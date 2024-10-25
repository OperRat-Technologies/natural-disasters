package me.tcklpl.naturaldisaster.schematics

import me.tcklpl.naturaldisaster.NaturalDisaster
import org.apache.commons.lang.NullArgumentException
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.util.ArrayList
import java.util.Arrays
import java.util.Locale
import java.util.Objects
import java.util.concurrent.atomic.AtomicInteger
import java.util.function.Consumer

class SchematicManager {
    private val schematics = ArrayList<Schematic>()
    private val schematicFolder: File = File(NaturalDisaster.instance.dataFolder, "schematics")

    init {
        if (schematicFolder.mkdirs()) NaturalDisaster.instance.logger.info("Criada a pasta dos schematics")
        loadSchematics()
    }

    private fun loadSchematics() {
        NaturalDisaster.instance.logger.info("Carregando schematics...")
        val currentSchematicIndex = AtomicInteger(1)
        Arrays.stream<File>(schematicFolder.listFiles())
            .filter { f: File -> !f.isDirectory() && f.getName().endsWith(".schematic") }.forEach { f: File ->
                try {
                    FileInputStream(f).use { fis ->
                        ObjectInputStream(fis).use { ois ->
                            val s = ois.readObject() as Schematic
                            s.buildBlockData()
                            schematics.add(s)
                        }
                    }
                } catch (_: IOException) {
                    NaturalDisaster.instance.logger.warning("Falha ao carregar schematic " + f.getName())
                } catch (_: ClassNotFoundException) {
                    NaturalDisaster.instance.logger.warning("Falha ao carregar schematic " + f.getName())
                }
                currentSchematicIndex.getAndIncrement()
            }
    }

    fun saveSchematics() {
        NaturalDisaster.instance.logger.info("Salvando schematics...")
        schematics.forEach(Consumer { s: Schematic ->
            val fileName = s.name.lowercase(Locale.getDefault()).replace(" ", "_") + ".schematic"
            val schematicFile = File(schematicFolder, fileName)
            if (!schematicFile.exists()) {
                try {
                    FileOutputStream(schematicFile).use { fos ->
                        ObjectOutputStream(fos).use { oos ->
                            oos.writeObject(s)
                        }
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        })
    }

    fun isNameAvailable(name: String?): Boolean {
        return schematics.stream().noneMatch { x: Schematic? -> x!!.name.equals(name, ignoreCase = true) }
    }

    fun registerSchematic(schematic: Schematic) {
        schematics.add(schematic)
    }

    fun getSchematicByName(name: String): Schematic? {
        return schematics.stream().filter { x: Schematic -> x.name.equals(name, ignoreCase = true) }.findFirst()
            .orElse(null)
    }

    fun loadSchematicAt(
        location: Location?,
        schematic: Schematic?,
        keepSchematicAir: Boolean,
        loadPosition: SchematicLoadPosition
    ) {
        if (location == null) throw NullArgumentException("Location cannot be null")
        if (schematic == null) throw NullArgumentException("Schematic cannot be null")

        var schematicX = location.blockX
        var schematicY = location.blockY
        var schematicZ = location.blockZ

        when (loadPosition) {
            SchematicLoadPosition.FLOOR_CENTER -> {
                schematicX -= Math.floorDiv(schematic.width, 2)
                schematicZ -= Math.floorDiv(schematic.lenght, 2)
            }

            SchematicLoadPosition.TRUE_CENTER -> {
                schematicX -= Math.floorDiv(schematic.width, 2)
                schematicY -= Math.floorDiv(schematic.height, 2)
                schematicZ -= Math.floorDiv(schematic.lenght, 2)
            }

            SchematicLoadPosition.LOWEST_COORDINATES -> {}
        }

        var currentBlockIndex = 0
        for (x in schematicX..(schematicX + schematic.width)) {
            for (y in schematicY..(schematicY + schematic.height)) {
                for (z in schematicZ..(schematicZ + schematic.lenght)) {
                    if (!keepSchematicAir && schematic.blocks[currentBlockIndex] === Material.AIR) {
                        currentBlockIndex++
                        continue
                    }
                    val b = Objects.requireNonNull<World?>(location.world).getBlockAt(x, y, z)
                    b.type = schematic.blocks[currentBlockIndex]
                    b.blockData = schematic.blockData[currentBlockIndex]
                    currentBlockIndex++
                }
            }
        }
    }
}
