package me.tcklpl.naturaldisaster.player.skins

import me.tcklpl.naturaldisaster.NaturalDisaster
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.util.ArrayList

class SkinManager(private val main: JavaPlugin) {
    private val skinsFile: File = File(main.dataFolder, "skins.yml")
    val managedSkins: MutableList<CustomSkin> = ArrayList<CustomSkin>()

    fun setupSkins() {
        if (!skinsFile.exists()) return
        val skinsConfig = YamlConfiguration.loadConfiguration(skinsFile)
        val skins = skinsConfig.getConfigurationSection("skins")
        if (skins == null) return

        for (skinName in skins.getKeys(false)) {
            val value = skinsConfig.getString("skins.$skinName.value")
            val signature = skinsConfig.getString("skins.$skinName.signature")
            managedSkins.add(CustomSkin(skinName, value!!, signature!!))
        }
        NaturalDisaster.instance.logger.info("Carregadas ${managedSkins.size} skins")
    }

    fun getSkin(name: String): CustomSkin? {
        for (cs in managedSkins) {
            if (cs.name.equals(name, ignoreCase = true)) return cs
        }
        return null
    }

}
