package me.tcklpl.naturaldisaster;

import me.tcklpl.naturaldisaster.admin.AdminInventoryClick;
import me.tcklpl.naturaldisaster.admin.ArenaAdmin;
import me.tcklpl.naturaldisaster.auth.AuthCommands;
import me.tcklpl.naturaldisaster.auth.AuthManager;
import me.tcklpl.naturaldisaster.commands.*;
import me.tcklpl.naturaldisaster.config.ConfigManager;
import me.tcklpl.naturaldisaster.config.globalconfigs.GameConfig;
import me.tcklpl.naturaldisaster.database.Database;
import me.tcklpl.naturaldisaster.events.*;
import me.tcklpl.naturaldisaster.events.arena.Damage;
import me.tcklpl.naturaldisaster.events.arena.Death;
import me.tcklpl.naturaldisaster.events.arena.EntityChangeBlock;
import me.tcklpl.naturaldisaster.player.cPlayer.CPlayerManager;
import me.tcklpl.naturaldisaster.player.friends.FriendsGUI;
import me.tcklpl.naturaldisaster.player.skins.RefreshSkin;
import me.tcklpl.naturaldisaster.player.skins.SkinManager;
import me.tcklpl.naturaldisaster.shop.ShopCommand;
import me.tcklpl.naturaldisaster.shop.ShopInventoryClick;
import me.tcklpl.naturaldisaster.worlds.WorldCommands;
import me.tcklpl.naturaldisaster.worlds.WorldManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.*;

public class NaturalDisaster extends JavaPlugin {

    WorldManager worldManager;
    private static Database database;
    private static JavaPlugin mainReference;
    private static AuthManager authManager;
    private static CPlayerManager cPlayerManager;
    private static GameManager gameManager;
    private static SkinManager skinManager;
    private static ConfigManager configManager;

    @Override
    public void onEnable() {

        mainReference = this;
        authManager = new AuthManager();

        configManager = new ConfigManager(this);

        List<String> managedWorlds = getConfig().getStringList("worlds");
        worldManager = new WorldManager(managedWorlds);

        gameManager = new GameManager(this);
        gameManager.setCurrentStatus(GameStatus.IN_LOBBY);

        skinManager = new SkinManager(this);
        skinManager.setupSkins();

        cPlayerManager = new CPlayerManager();
        database = new Database("localhost", 3306, "natural_disaster", "nds", "Bsr7lbh2zv91oWDp");
        cPlayerManager.loadPlayers();

        registerEvents();
        registerCommands();

    }

    @Override
    public void onDisable() {

        getConfig().set("worlds", worldManager.getManagedWorlds());
        gameManager.getArenaManager().saveArenas();
        cPlayerManager.savePlayers();
        try {
            NaturalDisaster.getSkinManager().saveSkins();
        } catch (IOException e) {
            e.printStackTrace();
        }
        configManager.saveAllConfigs();
        saveConfig();
    }

    private void registerEvents() {

        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new Motd(), this);
        pm.registerEvents(new Join(this), this);
        pm.registerEvents(new Death(), this);
        pm.registerEvents(new PickItem(), this);
        pm.registerEvents(new FoodLevel(), this);
        pm.registerEvents(new IceMelt(), this);
        pm.registerEvents(new Leave(), this);
        pm.registerEvents(new Move(), this);
        pm.registerEvents(new AdminInventoryClick(), this);
        pm.registerEvents(new Chat(), this);
        pm.registerEvents(new Damage(), this);
        pm.registerEvents(new Chair(), this);
        pm.registerEvents(new MobSpawn(), this);
        pm.registerEvents(new ShopInventoryClick(), this);
        pm.registerEvents(new FriendsGUI(), this);
        pm.registerEvents(new EntityChangeBlock(), this);

    }

    private void registerCommands() {

        Objects.requireNonNull(getCommand("world")).setExecutor(new WorldCommands(worldManager));
        GamemodeCommands gmc = new GamemodeCommands();
        Objects.requireNonNull(getCommand("creative")).setExecutor(gmc);
        Objects.requireNonNull(getCommand("survival")).setExecutor(gmc);
        Objects.requireNonNull(getCommand("adventure")).setExecutor(gmc);
        Objects.requireNonNull(getCommand("spectator")).setExecutor(gmc);
        Objects.requireNonNull(getCommand("arena")).setExecutor(new MapCreator());
        Objects.requireNonNull(getCommand("start")).setExecutor(new Start(this));
        Objects.requireNonNull(getCommand("balance")).setExecutor(new Balance());
        Objects.requireNonNull(getCommand("admin")).setExecutor(new ArenaAdmin());
        Objects.requireNonNull(getCommand("sch")).setExecutor(new SchematicCommand());
        Objects.requireNonNull(getCommand("heal")).setExecutor(new Heal());
        Objects.requireNonNull(getCommand("shop")).setExecutor(new ShopCommand());
        Objects.requireNonNull(getCommand("friends")).setExecutor(new FriendsGUI());
        Objects.requireNonNull(getCommand("refresh")).setExecutor(new RefreshSkin());
        AuthCommands authCommands = new AuthCommands();
        Objects.requireNonNull(getCommand("register")).setExecutor(authCommands);
        Objects.requireNonNull(getCommand("login")).setExecutor(authCommands);

    }

    public static AuthManager getAuthManager() { return authManager; }

    public static JavaPlugin getMainReference() {
        return mainReference;
    }

    public static Database getDatabase() {
        return database;
    }

    public static CPlayerManager getPlayerManager() { return  cPlayerManager; }

    public static AuthManager getAuthenticationManager() {
        return authManager;
    }

    public static GameManager getGameManager() {
        return gameManager;
    }

    public static SkinManager getSkinManager() { return skinManager; }

    public static ConfigManager getConfigManager() {
        return configManager;
    }
}
