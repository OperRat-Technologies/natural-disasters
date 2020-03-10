package me.tcklpl.naturaldisaster.map;

import me.tcklpl.naturaldisaster.GameStatus;
import me.tcklpl.naturaldisaster.NaturalDisaster;
import me.tcklpl.naturaldisaster.disasters.*;
import me.tcklpl.naturaldisaster.player.cPlayer.CPlayer;
import me.tcklpl.naturaldisaster.player.ingamePlayer.ArenaPlayerManager;
import me.tcklpl.naturaldisaster.player.skins.SkinManager;
import me.tcklpl.naturaldisaster.util.ActionBar;
import me.tcklpl.naturaldisaster.util.NamesAndColors;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.reflections.Reflections;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

public class MapManager {

    private JavaPlugin mainReference;

    private List<DisasterMap> arenas = new ArrayList<>();
    private List<Disaster> disasters = new ArrayList<>();
    private DisasterMap currentMap;
    private Disaster currentDisaster;
    private int counterId;
    private GameStatus currentStatus;

    private ArenaPlayerManager arenaPlayerManager;

    public MapManager(JavaPlugin mainReference) {
        this.mainReference = mainReference;
    }

    public GameStatus getCurrentStatus() {
        return currentStatus;
    }

    public void setCurrentStatus(GameStatus currentStatus) {
        this.currentStatus = currentStatus;
    }

    public void registerArena(DisasterMap map) {
        if (arenas.stream().noneMatch(map::equals)) {
            arenas.add(map);
        }
    }

    public void setMainReference(JavaPlugin mainReference) {
        this.mainReference = mainReference;
    }

    public List<DisasterMap> getAllArenas() {
        return arenas;
    }

    public DisasterMap getMapByName(String name) {
        for (DisasterMap map : arenas)
            if (map.getName().equalsIgnoreCase(name))
                return map;
        return null;
    }

    public DisasterMap getPlayerMap(Player player) {
        for (DisasterMap map : arenas) {
            if (map.getPlayersInArena().contains(player))
                return map;
        }
        return null;
    }

    public void setupDisasters() {
        Reflections reflections = new Reflections("me.tcklpl.naturaldisaster.disasters");
        Set<Class<? extends Disaster>> disasterClasses = reflections.getSubTypesOf(Disaster.class);
        try {
            for (Class<? extends Disaster> disasterClass : disasterClasses) {
                disasters.add(disasterClass.getConstructor(DisasterMap.class, JavaPlugin.class).newInstance(null, mainReference));
            }
        } catch (InstantiationException | InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
            e.printStackTrace();
        }
        NaturalDisaster.getMainReference().getLogger().info("Carregados " + disasterClasses.size() + " desastres");
    }

    public void randomNextMap() {
        Random random = new Random();

        if (currentMap == null) {
            currentMap = arenas.get(random.nextInt(arenas.size()));
        }

        NaturalDisaster.getMainReference().getLogger().info("Carregando próximo mapa: " + currentMap.getName());
        ActionBar ab = new ActionBar(ChatColor.GOLD + "Próximo mapa: " + currentMap.getName());
        ab.sendToAll();

        // Load world
        World w = Bukkit.createWorld(new WorldCreator(currentMap.getName()));
        assert w != null;
        w.setAutoSave(false);

        w.setStorm(false);

        // Update locations with current world object
        currentMap.updateArenaWorld(w);

        if (currentDisaster == null) {
            currentDisaster = disasters.get(random.nextInt(disasters.size()));
        }

        assert currentDisaster != null;
        currentDisaster.setMap(currentMap);

        NaturalDisaster.getMainReference().getLogger().info("Próximo desastre: " + currentDisaster.getName());
    }

    public void setupArenas() {
        int arenas = 0;
        if (mainReference.getConfig().getConfigurationSection("arenas") != null)
            for (String arena : Objects.requireNonNull(mainReference.getConfig().getConfigurationSection("arenas")).getKeys(false)) {
                arenas++;
                String path = "arenas." + arena;
                int pos1X = mainReference.getConfig().getInt(path + ".pos1.x");
                int pos1Y = mainReference.getConfig().getInt(path + ".pos1.y");
                int pos1Z = mainReference.getConfig().getInt(path + ".pos1.z");
                int pos2X = mainReference.getConfig().getInt(path + ".pos2.x");
                int pos2Y = mainReference.getConfig().getInt(path + ".pos2.y");
                int pos2Z = mainReference.getConfig().getInt(path + ".pos2.z");

                Location pos1 = new Location(null, pos1X, pos1Y, pos1Z);
                Location pos2 = new Location(null, pos2X, pos2Y, pos2Z);
                List<Location> spawns = new ArrayList<>();

                for (String spawnCode : Objects.requireNonNull(mainReference.getConfig().getConfigurationSection(path + ".spawns")).getKeys(false)) {
                    String spawnName = path + ".spawns." + spawnCode;
                    int spawnX = mainReference.getConfig().getInt(spawnName + ".x");
                    int spawnY = mainReference.getConfig().getInt(spawnName + ".y");
                    int spawnZ = mainReference.getConfig().getInt(spawnName + ".z");
                    Location spawnLoc = new Location(null, spawnX, spawnY, spawnZ);
                    spawns.add(spawnLoc);
                }

                DisasterMap map = new DisasterMap(mainReference, pos1, pos2, arena, spawns);
                registerArena(map);
            }
        NaturalDisaster.getMainReference().getLogger().info("Carregadas " + arenas + " arenas");
    }

    public void saveArenas() {
        for (DisasterMap map : getAllArenas()) {
            try {
                mainReference.getConfig().set("arenas." + map.getName() + ".pos1.x", map.getPos1().getBlockX());
                mainReference.getConfig().set("arenas." + map.getName() + ".pos1.y", map.getPos1().getBlockY());
                mainReference.getConfig().set("arenas." + map.getName() + ".pos1.z", map.getPos1().getBlockZ());
                mainReference.getConfig().set("arenas." + map.getName() + ".pos2.x", map.getPos2().getBlockX());
                mainReference.getConfig().set("arenas." + map.getName() + ".pos2.y", map.getPos2().getBlockY());
                mainReference.getConfig().set("arenas." + map.getName() + ".pos2.z", map.getPos2().getBlockZ());

                int count = 0;
                for (Location loc : map.getSpawns()) {
                    mainReference.getConfig().set("arenas." + map.getName() + ".spawns.spawn" + count + ".x", loc.getBlockX());
                    mainReference.getConfig().set("arenas." + map.getName() + ".spawns.spawn" + count + ".y", loc.getBlockY());
                    mainReference.getConfig().set("arenas." + map.getName() + ".spawns.spawn" + count + ".z", loc.getBlockZ());
                    count++;
                }
            } catch (NullPointerException e) {
                NaturalDisaster.getMainReference().getLogger().log(Level.WARNING, "Erro ao salvar arena " + map.getName() + ", mapa provavelmente não carregado");
            }
        }
    }

    private void cancelStartupCounterTask() {
        Bukkit.getScheduler().cancelTask(counterId);
    }

    public void startNextGame() {
        if (currentMap != null && currentDisaster != null) {

            arenaPlayerManager = new ArenaPlayerManager(mainReference);
            currentStatus = GameStatus.STARTING;
            currentMap.addAllPlayersToArena();

            assignRandomNames();

            AtomicInteger counter = new AtomicInteger(5);
            counterId = Bukkit.getScheduler().scheduleSyncRepeatingTask(mainReference, () -> {
                if (counter.get() == 0)
                    cancelStartupCounterTask();
                ActionBar ab = new ActionBar(ChatColor.GOLD + "Começando em: " + counter.get());
                ab.sendToAll();
                counter.getAndDecrement();
            }, 20L, 20L);

            currentMap.teleportPlayersToSpawns();

            Bukkit.getScheduler().scheduleSyncDelayedTask(mainReference, () -> {
                currentDisaster.startDisaster();
                ActionBar ab = new ActionBar(ChatColor.RED + "Boa sorte!");
                ab.sendToAll();
                currentStatus = GameStatus.IN_GAME;
                for (Player all : currentMap.getPlayersInArena())
                    all.playSound(all.getLocation(), Sound.EVENT_RAID_HORN, 1f, 1f);
                Bukkit.broadcastMessage(ChatColor.GRAY + currentDisaster.getHint());
            }, 130L);
        }
    }

    public void finishGame() {
        currentDisaster.stopDisaster();

        for (Player p : currentMap.getPlayersInArena())
            arenaPlayerManager.returnPlayerToNormal(p);

        for (Player p : Bukkit.getOnlinePlayers()) {
            p.setDisplayName(p.getName());
            p.setPlayerListName(p.getName());
            p.setScoreboard(Objects.requireNonNull(Bukkit.getScoreboardManager()).getNewScoreboard());
        }

        NaturalDisaster.getSkinManager().applyAfterGameSkinChanges();

        // Wait 1s to teleport 'cause players may still be ticking
        Bukkit.getScheduler().scheduleSyncDelayedTask(mainReference, () -> {
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.setGameMode(GameMode.ADVENTURE);
                p.teleport(new Location(Bukkit.getWorld("void"), 8, 8, 8));
                p.setHealth(20);
                p.setFoodLevel(20);
                p.setFireTicks(0);
                p.setFallDistance(0);
                p.getInventory().clear();
                for (PotionEffect pe : p.getActivePotionEffects())
                    p.removePotionEffect(pe.getType());
            }
        }, 20L);

        // Wait 6s to unload map in order to give time to all unfinished delayed schedules
        Bukkit.getScheduler().scheduleSyncDelayedTask(mainReference, () -> {
            if (Bukkit.unloadWorld(Objects.requireNonNull(currentMap.getPos1().getWorld()), false)) {
                NaturalDisaster.getMainReference().getLogger().info("Mundo " + currentMap.getName() + " descarregado.");
            } else NaturalDisaster.getMainReference().getLogger().severe("Falha ao descarregar mundo " + currentMap);
            currentMap = null;
            currentDisaster = null;
            currentStatus = GameStatus.IN_LOBBY;
        }, 120L);
    }

    public void teleportSpectatorToArena(Player p) {
        p.setGameMode(GameMode.SPECTATOR);
        if (currentMap != null)
            if (currentMap.getPlayersInArena().size() > 0)
                p.teleport(currentMap.getPlayersInArena().get(0).getLocation());
    }

    public void updateArenaForDeadPlayer(Player p) {

        arenaPlayerManager.returnPlayerToNormal(p);
        String name = p.getName();

        if (currentMap.getPlayersInArena().contains(p)) {
            currentMap.getPlayersInArena().remove(p);
            // If the game ends
            if (currentMap.getPlayersInArena().size() <= 1) {
                Bukkit.broadcastMessage(ChatColor.YELLOW + "O jogo acabou.");
                if (currentMap.getPlayersInArena().size() == 1) {
                    Bukkit.getScheduler().runTaskLater(mainReference, () -> Bukkit.broadcastMessage(ChatColor.GREEN + currentMap.getPlayersInArena().get(0).getName() + " venceu!"), 20L);
                    Player winner = currentMap.getPlayersInArena().get(0);

                    CPlayer cp = NaturalDisaster.getPlayerManager().getCPlayer(winner.getUniqueId());
                    cp.getPlayerData().setWins(cp.getPlayerData().getWins() + 1);
                    cp.getPlayerData().setMoney(cp.getPlayerData().getMoney() + 25);
                    winner.sendMessage(ChatColor.GOLD + "+$25 por ganhar a partida.");
                }
                finishGame();
            } else {
                Bukkit.broadcastMessage(p.getDisplayName() + ChatColor.GRAY + "( " + name + " ) morreu, ainda restam " + currentMap.getPlayersInArena().size() + " jogadores vivos!");
                p.setGameMode(GameMode.SPECTATOR);
                teleportSpectatorToArena(p);
                p.playSound(p.getLocation(), Sound.BLOCK_BEACON_DEACTIVATE, 1f, 1f);
                for (Player player : currentMap.getPlayersInArena()) {
                    player.sendMessage(ChatColor.GRAY + "+$1 por sobreviver.");

                    CPlayer cp = NaturalDisaster.getPlayerManager().getCPlayer(player.getUniqueId());
                    cp.getPlayerData().setMoney(cp.getPlayerData().getMoney() + 1);
                }
            }
        }
    }

    public void arenaTimeout() {

        for (Player p : currentMap.getPlayersInArena()) {
            CPlayer cp = NaturalDisaster.getPlayerManager().getCPlayer(p.getUniqueId());
            cp.getPlayerData().setWins(cp.getPlayerData().getWins() + 1);
            cp.getPlayerData().setMoney(cp.getPlayerData().getMoney() + 25);
            p.sendMessage(ChatColor.GOLD + "+$25 por ganhar a partida.");
        }

        Bukkit.broadcastMessage(ChatColor.YELLOW + "Acabou o tempo do mapa, todos ainda vivos ganharam");
        finishGame();
    }

    public boolean isIsInGame() { return currentStatus == GameStatus.IN_GAME || currentStatus == GameStatus.STARTING; }

    public void setCurrentMap(DisasterMap map) {
        if (currentStatus == GameStatus.IN_LOBBY)
            currentMap = map;
    }

    public void setCurrentDisaster(Disaster disaster) {
        if (currentStatus == GameStatus.IN_LOBBY)
            currentDisaster = disaster;
    }

    public List<DisasterMap> getAllMaps() {
        return arenas;
    }

    public List<Disaster> getAllDisasters() {
        return disasters;
    }

    public Disaster getDisasterByName(String name) {
        for (Disaster dis : disasters) {
            if (dis.getName().equalsIgnoreCase(name))
                return dis;
        }
        return null;
    }

    public void assignRandomNames() {
        if (currentMap != null) {

            int playerCount = currentMap.getPlayersInArena().size();
            List<String> names = NamesAndColors.pickRandomNames(playerCount);
            List<ChatColor> colors = NamesAndColors.pickRandomColors(playerCount);
            for (int i = 0; i < playerCount; i++) {
                Player p = currentMap.getPlayersInArena().get(i);
                assert p != null;
                arenaPlayerManager.disguisePlayer(p, colors.get(i).toString() + names.get(i));
            }
        }
    }
}
