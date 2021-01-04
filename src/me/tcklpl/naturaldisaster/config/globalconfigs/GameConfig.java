package me.tcklpl.naturaldisaster.config.globalconfigs;

import me.tcklpl.naturaldisaster.config.*;
import org.bukkit.Material;

@Configuration(file = "game_config", access = ConfigAccess.OP, scope = ConfigScope.GLOBAL, icon = Material.BEDROCK)
public class GameConfig implements NDConfig {

    @ConfigField(name = "auto_start")
    private boolean autoStart = true;

    @ConfigField(name = "min_players_to_start", min = 1, max = 20)
    private int minPlayersToStart = 5;

    @ConfigField(name = "players_to_start", min = 2, max = 24)
    private int playersToStart = 10;

    @ConfigField(name = "long_start_timer_secs", min = 10, max = 300)
    private int longStartTimerSeconds = 120;

    @ConfigField(name = "short_start_timer_secs", min = 5, max = 60)
    private int shortStartTimerSeconds = 30;

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

    public void setAutoStart(boolean autoStart) {
        this.autoStart = autoStart;
    }

    public void setMinPlayersToStart(int minPlayersToStart) {
        this.minPlayersToStart = minPlayersToStart;
    }

    public void setPlayersToStart(int playersToStart) {
        this.playersToStart = playersToStart;
    }

    public void setLongStartTimerSeconds(int longStartTimerSeconds) {
        this.longStartTimerSeconds = longStartTimerSeconds;
    }

    public void setShortStartTimerSeconds(int shortStartTimerSeconds) {
        this.shortStartTimerSeconds = shortStartTimerSeconds;
    }
}
