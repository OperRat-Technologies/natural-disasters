package me.tcklpl.naturaldisaster.util

import org.bukkit.ChatColor
import java.util.ArrayList

object NamesAndColors {

    private val phoneticAlphabet = mutableListOf(
        "Alfa", "Bravo", "Charlie", "Delta", "Echo",
        "Foxtrot", "Golf", "Hotel", "India", "Juliett", "Kilo", "Lima", "Mike", "November", "Oscar", "Papa",
        "Quebec", "Romeo", "Sierra", "Tango", "Uniform", "Victor", "Whiskey", "X-Ray", "Yankee", "Zulu"
    )

    private val usedColors = mutableListOf(
        ChatColor.BLUE, ChatColor.GREEN, ChatColor.YELLOW,
        ChatColor.LIGHT_PURPLE, ChatColor.GRAY, ChatColor.RED
    )

    /**
     * Picks x random non-repeationg names from above list
     * @param count the number of names to be picked
     * @return the list of picked non-repeating names
     */
    fun pickRandomNames(count: Int): MutableList<String?> {
        val res: MutableList<String?> = ArrayList<String?>()
        phoneticAlphabet.shuffle()
        for (i in 0 until count) {
            res.add(phoneticAlphabet[i])
        }
        return res
    }

    /**
     * Picks random colors from above list
     * @param count the number of colors to be picked
     * @return the list of picked colors
     */
    fun pickRandomColors(count: Int): MutableList<ChatColor?> {
        val res: MutableList<ChatColor?> = ArrayList<ChatColor?>()
        usedColors.shuffle()
        for (i in 0 until count) {
            res.add(usedColors[i % usedColors.size])
        }
        return res
    }
}
