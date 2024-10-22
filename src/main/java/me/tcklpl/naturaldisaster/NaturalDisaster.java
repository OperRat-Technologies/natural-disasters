package me.tcklpl.naturaldisaster;

import me.tcklpl.naturaldisaster.admin.AdminInventoryClickEvent;
import me.tcklpl.naturaldisaster.admin.ArenaAdminCmd;
import me.tcklpl.naturaldisaster.commands.*;
import me.tcklpl.naturaldisaster.events.*;
import me.tcklpl.naturaldisaster.events.arena.DamageEvent;
import me.tcklpl.naturaldisaster.events.arena.DeathEvent;
import me.tcklpl.naturaldisaster.player.cPlayer.CPlayerManager;
import me.tcklpl.naturaldisaster.player.friends.FriendsGUI;
import me.tcklpl.naturaldisaster.player.skins.RefreshSkin;
import me.tcklpl.naturaldisaster.player.skins.SkinManager;
import me.tcklpl.naturaldisaster.schematics.SchematicManager;
import me.tcklpl.naturaldisaster.shop.ShopCommand;
import me.tcklpl.naturaldisaster.shop.ShopInventoryClick;
import me.tcklpl.naturaldisaster.worlds.WorldCommands;
import me.tcklpl.naturaldisaster.worlds.WorldManager;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class NaturalDisaster extends JavaPlugin {

    WorldManager worldManager;
    private static NaturalDisaster mainReference;
    private static CPlayerManager cPlayerManager;
    private static GameManager gameManager;
    private static SkinManager skinManager;
    private static SchematicManager schematicManager;

    private static final Random random = new Random();

    @Override
    public void onEnable() {

        mainReference = this;

        List<String> managedWorlds = getConfig().getStringList("worlds");
        worldManager = new WorldManager(managedWorlds);

        gameManager = new GameManager(this);
        gameManager.setCurrentStatus(GameStatus.IN_LOBBY);

        skinManager = new SkinManager(this);
        skinManager.setupSkins();

        cPlayerManager = new CPlayerManager();
        cPlayerManager.loadPlayers();

        schematicManager = new SchematicManager();

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
        saveConfig();

        schematicManager.saveSchematics();
    }

    private void registerEvents() {
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(MotdEvent.INSTANCE, this);
        pm.registerEvents(JoinEvent.INSTANCE, this);
        pm.registerEvents(DeathEvent.INSTANCE, this);
        pm.registerEvents(PickItemEvent.INSTANCE, this);
        pm.registerEvents(FoodLevelEvent.INSTANCE, this);
        pm.registerEvents(IceMeltEvent.INSTANCE, this);
        pm.registerEvents(LeaveEvent.INSTANCE, this);
        pm.registerEvents(MoveEvent.INSTANCE, this);
        pm.registerEvents(AdminInventoryClickEvent.INSTANCE, this);
        pm.registerEvents(ChatEvent.INSTANCE, this);
        pm.registerEvents(DamageEvent.INSTANCE, this);
        pm.registerEvents(MobSpawnEvent.INSTANCE, this);
        pm.registerEvents(new ShopInventoryClick(), this);
        pm.registerEvents(new FriendsGUI(), this);
    }

    private void registerCommands() {
        Objects.requireNonNull(getCommand("world")).setExecutor(new WorldCommands(worldManager));
        Objects.requireNonNull(getCommand("creative")).setExecutor(GamemodeCmd.INSTANCE);
        Objects.requireNonNull(getCommand("survival")).setExecutor(GamemodeCmd.INSTANCE);
        Objects.requireNonNull(getCommand("adventure")).setExecutor(GamemodeCmd.INSTANCE);
        Objects.requireNonNull(getCommand("spectator")).setExecutor(GamemodeCmd.INSTANCE);
        Objects.requireNonNull(getCommand("arena")).setExecutor(new MapCreator());
        Objects.requireNonNull(getCommand("start")).setExecutor(StartCmd.INSTANCE);
        Objects.requireNonNull(getCommand("balance")).setExecutor(BalanceCmd.INSTANCE);
        Objects.requireNonNull(getCommand("admin")).setExecutor(ArenaAdminCmd.INSTANCE);
        Objects.requireNonNull(getCommand("admin")).setTabCompleter(ArenaAdminCmd.INSTANCE);
        Objects.requireNonNull(getCommand("sch")).setExecutor(new SchematicCreator());
        Objects.requireNonNull(getCommand("heal")).setExecutor(HealCmd.INSTANCE);
        Objects.requireNonNull(getCommand("shop")).setExecutor(new ShopCommand());
        Objects.requireNonNull(getCommand("friends")).setExecutor(new FriendsGUI());
        Objects.requireNonNull(getCommand("refresh")).setExecutor(new RefreshSkin());
    }

    public WorldManager getWorldManager() {
        return worldManager;
    }

    public static NaturalDisaster getMainReference() {
        return mainReference;
    }

    public static CPlayerManager getPlayerManager() {
        return cPlayerManager;
    }

    public static GameManager getGameManager() {
        return gameManager;
    }

    public static SkinManager getSkinManager() {
        return skinManager;
    }

    public static SchematicManager getSchematicManager() {
        return schematicManager;
    }

    public static Random getRandom() {
        return random;
    }
}
