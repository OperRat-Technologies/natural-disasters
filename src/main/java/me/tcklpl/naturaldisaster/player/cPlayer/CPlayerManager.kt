package me.tcklpl.naturaldisaster.player.cPlayer

import me.tcklpl.naturaldisaster.NaturalDisaster
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.ArrayList
import java.util.Objects
import java.util.UUID

class CPlayerManager {
    private val managedPlayers = ArrayList<CPlayer>()

    fun getCPlayer(uuid: UUID?): CPlayer? {
        for (cp in managedPlayers) if (cp.uuid == uuid) return cp
        return null
    }

    fun getCPlayer(name: String?): CPlayer? {
        for (cp in managedPlayers) if (cp.name == name) return cp
        return null
    }

    fun registerCPlayer(cp: CPlayer?): Boolean {
        return managedPlayers.add(cp!!)
    }

    fun loadPlayers() {
        val playerFolder = File(NaturalDisaster.instance.dataFolder.toString() + "/players")
        if (playerFolder.exists() && playerFolder.isDirectory()) {
            try {
                Files.walk(Paths.get(NaturalDisaster.instance.dataFolder.toString() + "/players"))
                    .use { walk ->
                        val result = walk.map<String?> { obj: Path? -> obj.toString() }
                            .filter { f: String? -> f!!.endsWith(".player.yaml") }.toList()
                        for (playerFileName in result) {
                            val playerFile = File(playerFileName)

                            val config = YamlConfiguration.loadConfiguration(playerFile)
                            val name = config.getString("name")
                            val uuid = UUID.fromString(Objects.requireNonNull<String?>(config.getString("uuid")))
                            val money = config.getDouble("money")
                            val wins = config.getInt("wins")

                            checkNotNull(name)
                            val cp = CPlayer(uuid, name, wins, money)

                            managedPlayers.add(cp)
                        }
                        NaturalDisaster.instance.logger.info("Carregados " + result.size + " jogadores")
                    }
            } catch (_: IOException) {
                NaturalDisaster.instance.logger.warning("ERRO AO CARREGAR JOGADORES")
            }
        }
    }

    fun savePlayers() {
        if (!managedPlayers.isEmpty()) {
            val playerFolder = File(NaturalDisaster.instance.dataFolder.toString() + "/players")
            if (!(playerFolder.exists() && playerFolder.isDirectory())) {
                if (!playerFolder.mkdirs()) {
                    NaturalDisaster.instance.logger.warning("Falha ao criar diretório para players")
                    return
                }
            }

            var count = 0
            for (cp in managedPlayers) {
                val saveFile = File(
                    NaturalDisaster.instance.dataFolder.toString() + "/players", "${cp.name}.player.yaml"
                )
                if (saveFile.exists()) if (!saveFile.delete()) {
                    NaturalDisaster.instance.logger
                        .severe("NÃO FOI POSSÍVEL EXCLUIR ARQUIVO DO JOGADOR " + cp.name)
                    return
                }

                val playerConfig = YamlConfiguration()
                playerConfig.set("name", cp.name)
                playerConfig.set("uuid", cp.uuid.toString())
                playerConfig.set("money", cp.money)
                playerConfig.set("wins", cp.wins)

                try {
                    playerConfig.save(saveFile)
                    count++
                } catch (_: IOException) {
                    NaturalDisaster.instance.logger.warning("ERRO AO SALVAR JOGADOR ${cp.name}")
                }
            }
            NaturalDisaster.instance.logger.info("Salvos $count jogadores")
        }
    }
}
