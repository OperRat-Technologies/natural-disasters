package me.tcklpl.naturaldisaster.player.ingamePlayer

import com.mojang.authlib.GameProfile
import com.mojang.authlib.properties.Property
import me.tcklpl.naturaldisaster.NaturalDisaster
import me.tcklpl.naturaldisaster.reflection.ReflectionUtils
import org.bukkit.entity.Player
import java.util.HashMap
import java.util.UUID

class ArenaPlayerManager {
    private val arenaPlayers = HashMap<UUID, ArenaPlayer>()

    fun getPlayer(p: Player): ArenaPlayer? {
        return arenaPlayers.get(p.uniqueId)
    }

    /**
     * Disguises player (to be used in arena).
     * @param p the player to be disguised.
     * @param name the name to be applied to said player.
     */
    fun disguisePlayer(p: Player, name: String) {
        if (getPlayer(p) != null) {
            NaturalDisaster.instance.logger.warning("Usuário ${p.name} já tranformado")
            return
        }
        val profile = GameProfile(p.uniqueId, name)

        //TODO: choose textures from config
        profile.properties.put(
            "textures", Property(
                "textures",
                "eyJ0aW1lc3RhbXAiOjE1NjI2ODg3NjMyOTEsInByb2ZpbGVJZCI6ImIwZDczMmZlMDBmNzQwN2U5ZTdmNzQ2MzAxY2Q5OG" +
                        "NhIiwicHJvZmlsZU5hbWUiOiJPUHBscyIsInNpZ25hdHVyZVJlcXVpcmVkIjp0cnVlLCJ0ZXh0dXJlcyI6eyJTS0lOIjp" +
                        "7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMWE0YWY3MTg0NTVkNGFhYjUyOGU3YTYxZ" +
                        "jg2ZmEyNWU2YTM2OWQxNzY4ZGNiMTNmN2RmMzE5YTcxM2ViODEwYiJ9fX0=",
                "QBbXXjOaNTiwdAmYPy+mfQR7O2Cs2zqmOEL+eaP+bamTZ1ujPgxMOPt/xznX1LPdTa7JDR4AWlIr4HVGNl5MdEIqqxnZ" +
                        "ALGtzDeO57wfnNz0vtu2skt+JLmLjI2687Y1I2SOo9vdNx/b81T6mUDJ1t/dk1YTjPdBJuHAhKd4c52n12mchj5JzPGI5" +
                        "0r0KVcLtAhZeeKRXO3dwOnWNHdkyMe1A4DQdP2MCpZxQgX0NJmVPTW5oDCUY5QQF5WjxxUNiHdcBG3ruwzrSxNVjjyaOe" +
                        "RlFa6jDipk9wZZzqKMFBefrZ2deTf8Z29rNJIIK0WkOoYXCGZxLNZzUHfiIwdUNggWr0ccCh6KsNIZtJQiPIdiQPXO41+" +
                        "7iWMzc9EIWzZUwqDQk2Nq+/sZlHEVnsIh2knHqaezSA97oodNPnB34WtfsN8PDoLRI6r22S4fJelgn9SFUmQUiNn1fS6t" +
                        "8hBEnjnbZcHfcjQ+O8v3RcuEubkLg2ZzV6RiJtRHYNBmhr8jHmdEoADmxf78xjbGHyXr/U8+63DB/XlXjV0RXI4oGZNkxK" +
                        "NLP+Gg+uN4qN/H+noUEYnY4/Ls58ye/EuT7JuR0X1fSenOkY3x/L79K1cqrSi3EK1MmdTOwQAdiYi9HUUcGAf0Qv6YfDt7" +
                        "UHrUcTA5F3uR66TwJmPsDjWsNTBznFE="
            )
        )

        val originalGp = ReflectionUtils.cloneGameProfile(p)
        arenaPlayers.put(p.uniqueId, ArenaPlayer(p, name, originalGp, profile))

        ReflectionUtils.setPlayerGameProfile(p, getPlayer(p)!!.newProfile)
    }

    /**
     * Returns player to normal after game, removing disguise.
     * @param p the player.
     */
    fun returnPlayerToNormal(p: Player) {
        if (arenaPlayers.containsKey(p.uniqueId)) {
            ReflectionUtils.setPlayerGameProfile(p, getPlayer(p)!!.oldProfile)
            arenaPlayers.remove(p.uniqueId)
        }
    }
}
