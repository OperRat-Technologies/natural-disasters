package me.tcklpl.naturaldisaster.player;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class CustomPlayerManager {

    private static CustomPlayerManager INSTANCE;
    private JavaPlugin mainInstance;
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
        this.mainInstance = main;
    }

    public void setupPlayers() {
        playersFile = new File(mainInstance.getDataFolder(), "players.yml");
        if (playersFile.exists()) {
            playersConfig = YamlConfiguration.loadConfiguration(playersFile) ;
            if (mainInstance.getConfig().getConfigurationSection("players") != null) {
                for (String playerId : Objects.requireNonNull(mainInstance.getConfig().getConfigurationSection("players")).getKeys(false)) {
                    double money = mainInstance.getConfig().getDouble("players." + playerId + ".money");
                    managedPlayers.add(new MonetaryPlayer(Objects.requireNonNull(Bukkit.getPlayer(playerId)), money));
                }
            }
        }
    }

    public void savePlayers() throws IOException {
        if (playersConfig == null)
            playersConfig = new YamlConfiguration();
        for (MonetaryPlayer mp : managedPlayers) {
            playersConfig.set("players." + mp.getPlayer().getUniqueId() + ".money", mp.getMoney());
        }
        playersConfig.save(playersFile);

    }

    public MonetaryPlayer getMonetaryPlayer(UUID uniqueId) {
        for (MonetaryPlayer mp : managedPlayers) {
            if (mp.getPlayer().getUniqueId().equals(uniqueId))
                return mp;
        }
        return null;
    }

    public void registerPlayer(MonetaryPlayer mp) {
        managedPlayers.add(mp);
    }

}
