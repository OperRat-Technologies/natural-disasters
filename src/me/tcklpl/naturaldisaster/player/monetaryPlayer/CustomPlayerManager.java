package me.tcklpl.naturaldisaster.player.monetaryPlayer;

import me.tcklpl.naturaldisaster.player.cPlayer.PlayerData;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class CustomPlayerManager {

    private static CustomPlayerManager INSTANCE;
    private List<MonetaryPlayer> managedPlayers;
    private FileConfiguration playersConfig;
    private File playersFile;

    private CustomPlayerManager() {
        managedPlayers = new ArrayList<>();
    }

    public static CustomPlayerManager getInstance() {
        if (INSTANCE == null)
            INSTANCE = new CustomPlayerManager();
        return INSTANCE;
    }

    public void setMainInstance(JavaPlugin main) {
        playersFile = new File(main.getDataFolder(), "players.yml");
        if (playersFile.exists())
            playersConfig = YamlConfiguration.loadConfiguration(playersFile);
        else playersConfig = new YamlConfiguration();
    }

    public void setupPlayers() {
        if (playersFile.exists()) {
            if (playersConfig.getConfigurationSection("players") != null) {
                for (String playerId : Objects.requireNonNull(playersConfig.getConfigurationSection("players")).getKeys(false)) {
                    String name = playersConfig.getString("players." + playerId + ".name");
                    double money = playersConfig.getDouble("players." + playerId + ".money");
                    int wins = playersConfig.getInt("players." + playerId + ".wins");
                    int hints = playersConfig.getInt("players." + playerId + ".hints");
                    int respawns = playersConfig.getInt("players." + playerId + ".respawns");
                    List<UUID> friends = new ArrayList<>();
                    playersConfig.getStringList("players." + playerId + ".friends").forEach(friend -> friends.add(UUID.fromString(friend)));
                    List<UUID> requests = new ArrayList<>();
                    playersConfig.getStringList("players." + playerId + ".requests").forEach(request -> friends.add(UUID.fromString(request)));
                    List<UUID> blocks = new ArrayList<>();
                    playersConfig.getStringList("players." + playerId + ".blocks").forEach(block -> blocks.add(UUID.fromString(block)));

                    PlayerData playerData = new PlayerData(name, wins, hints, respawns, money, friends, requests, blocks);
                    playerData.setPlayerUUID(UUID.fromString(playerId));

                    managedPlayers.add(new MonetaryPlayer(UUID.fromString(playerId), playerData));
                }
            }
        }
    }

    public void savePlayers() throws IOException {
        for (MonetaryPlayer mp : managedPlayers) {
            playersConfig.set("players." + mp.getUUID().toString() + ".name", mp.getPlayerData().getName());
            playersConfig.set("players." + mp.getUUID().toString() + ".money", mp.getPlayerData().getMoney());
            playersConfig.set("players." + mp.getUUID().toString() + ".wins", mp.getPlayerData().getWins());
            playersConfig.set("players." + mp.getUUID().toString() + ".hints", mp.getPlayerData().getHints());
            playersConfig.set("players." + mp.getUUID().toString() + ".respawns", mp.getPlayerData().getRespawns());
            List<String> friends = new ArrayList<>();
            mp.getPlayerData().getFriends().forEach(friend -> friends.add(friend.toString()));
            playersConfig.set("players." + mp.getUUID().toString() + ".friends", friends);
            List<String> requests = new ArrayList<>();
            mp.getPlayerData().getFriendRequests().forEach(request -> requests.add(request.toString()));
            playersConfig.set("players." + mp.getUUID().toString() + ".requests", requests);
            List<String> blocks = new ArrayList<>();
            mp.getPlayerData().getBlocks().forEach(block -> blocks.add(block.toString()));
            playersConfig.set("players." + mp.getUUID().toString() + ".blocks", blocks);
        }
        playersConfig.save(playersFile);

    }

    public MonetaryPlayer getMonetaryPlayer(UUID uniqueId) {
        for (MonetaryPlayer mp : managedPlayers) {
            if (mp.getUUID().equals(uniqueId))
                return mp;
        }
        return null;
    }

    public MonetaryPlayer getMonetaryPlayer(String name) {
        for (MonetaryPlayer mp : managedPlayers) {
            if (mp.getPlayerData().getName().equalsIgnoreCase(name))
                return mp;
        }
        return null;
    }

    public void registerPlayer(MonetaryPlayer mp) {
        managedPlayers.add(mp);
    }

}
