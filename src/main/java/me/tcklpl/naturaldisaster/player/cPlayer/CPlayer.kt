package me.tcklpl.naturaldisaster.player.cPlayer

import java.util.UUID

data class CPlayer(val uuid: UUID, var name: String, var wins: Int, var money: Double)
