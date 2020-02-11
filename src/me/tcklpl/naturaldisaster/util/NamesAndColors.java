package me.tcklpl.naturaldisaster.util;

import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class NamesAndColors {

    private static Random r = new Random();
    private static List<String> phoneticAlphabet = new ArrayList<>(List.of("Alfa", "Bravo", "Charlie", "Delta", "Echo",
            "Foxtrot", "Golf", "Hotel", "India", "Juliett", "Kilo", "Lima", "Mike", "November", "Oscar", "Papa",
            "Quebec", "Romeo", "Sierra", "Tango", "Uniform", "Victor", "Whiskey", "X-Ray", "Yankee", "Zulu"));
    private static List<ChatColor> usedColors = new ArrayList<>(List.of(ChatColor.BLUE, ChatColor.GREEN, ChatColor.YELLOW,
            ChatColor.LIGHT_PURPLE, ChatColor.GRAY, ChatColor.RED));

    /**
     * Picks x random non-repeationg names from above list
     * @param count the number of names to be picked
     * @return the list of picked non-repeating names
     */
    public static List<String> pickRandomNames(int count) {
        List<String> res = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            String test;
            do {
                test = phoneticAlphabet.get(r.nextInt(phoneticAlphabet.size()));
            } while (res.contains(test));
            res.add(test);
        }
        return res;
    }

    /**
     * Picks random colors from above list
     * @param count the number of colors to be picked
     * @return the list of picked colors
     */
    public static List<ChatColor> pickRandomColors(int count) {
        List<ChatColor> res = new ArrayList<>();
        Collections.shuffle(usedColors);
        for (int i = 0; i < count; i++) {
            res.add(usedColors.get(i % usedColors.size()));
        }
        return res;
    }

}
