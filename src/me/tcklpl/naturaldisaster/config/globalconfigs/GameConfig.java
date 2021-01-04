package me.tcklpl.naturaldisaster.config.globalconfigs;

import me.tcklpl.naturaldisaster.config.*;
import org.bukkit.Material;

@Configuration(file = "game_config", access = ConfigAccess.OP, scope = ConfigScope.GLOBAL, icon = Material.BEDROCK)
public class GameConfig implements NDConfig {

    @ConfigField(name = "auto_start")
    private boolean autoStart;

    @ConfigField(name = "min_players_to_start", min = 1, max = 20)
    private int minPlayersToStart;

    @ConfigField(name = "players_to_start", min = 2, max = 24)
    private int playersToStart;

    @ConfigField(name = "long_start_timer_secs", min = 10, max = 300)
    private int longStartTimerSeconds;

    @ConfigField(name = "short_start_timer_secs", min = 5, max = 60)
    private int shortStartTimerSeconds;

    public GameConfig() {
    }

    @Override
    public String toString() {
        return "GameConfig{" +
                "autoStart=" + autoStart +
                ", minPlayersToStart=" + minPlayersToStart +
                ", playersToStart=" + playersToStart +
                ", longStartTimerSeconds=" + longStartTimerSeconds +
                ", shortStartTimerSeconds=" + shortStartTimerSeconds +
                '}';
    }

    @Override
    public void fillDefaults() {
        autoStart = true;
        minPlayersToStart = 5;
        playersToStart = 10;
        longStartTimerSeconds = 120;
        shortStartTimerSeconds = 30;
    }

    public boolean isAutoStart() {
        return autoStart;
    }

    public int getMinPlayersToStart() {
        return minPlayersToStart;
    }

    public int getPlayersToStart() {
        return playersToStart;
    }

    public int getLongStartTimerSeconds() {
        return longStartTimerSeconds;
    }

    public int getShortStartTimerSeconds() {
        return shortStartTimerSeconds;
    }
}
