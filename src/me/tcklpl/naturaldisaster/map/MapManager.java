package me.tcklpl.naturaldisaster.map;

import me.tcklpl.naturaldisaster.GameStatus;
import me.tcklpl.naturaldisaster.NaturalDisaster;
import me.tcklpl.naturaldisaster.disasters.*;
import me.tcklpl.naturaldisaster.player.cPlayer.CPlayer;
import me.tcklpl.naturaldisaster.player.ingamePlayer.ArenaPlayerManager;
import me.tcklpl.naturaldisaster.util.ActionBar;
import me.tcklpl.naturaldisaster.util.NamesAndColors;
import me.tcklpl.naturaldisaster.util.PlayerUtils;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.reflections.Reflections;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.stream.Stream;

public class MapManager {

    private final JavaPlugin mainReference;

    private final List<DisasterMap> arenas = new ArrayList<>();
    private final List<Disaster> disasters = new ArrayList<>();
    private final Queue<Disaster> lastDisasters = new LinkedList<>();
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
                disasters.add(disasterClass.getConstructor().newInstance());
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
        World w = Bukkit.createWorld(new WorldCreator(currentMap.getWorldName()));
        assert w != null;

        w.setAutoSave(false);

        w.setStorm(false);

        // Update locations with current world object
        currentMap.updateArenaWorld(w);

        if (currentDisaster == null) {

            if (lastDisasters.size() > 3)
                lastDisasters.remove();
            do {
                currentDisaster = disasters.get(random.nextInt(disasters.size()));
            } while (lastDisasters.contains(currentDisaster) || !currentDisaster.isPlayable());
            lastDisasters.add(currentDisaster);

        }

        assert currentDisaster != null;
        currentDisaster.setMap(currentMap);

        NaturalDisaster.getMainReference().getLogger().info("Próximo desastre: " + currentDisaster.getName());
    }

    public void setupArenas() {
        try (Stream<Path> arenaFiles = Files.walk(Path.of(new File(mainReference.getDataFolder(), "arenas").getPath()))) {
            AtomicInteger count = new AtomicInteger(0);
            arenaFiles.filter(Files::isRegularFile).forEach(config -> {

                FileConfiguration arenaConfig = YamlConfiguration.loadConfiguration(config.toFile());

                String name = arenaConfig.getString("name");
                String worldName = arenaConfig.getString("world");
                Material icon = Material.valueOf(arenaConfig.getString("icon"));

                int pos1x = arenaConfig.getInt("pos1.x");
                int pos1y = arenaConfig.getInt("pos1.y");
                int pos1z = arenaConfig.getInt("pos1.z");

                int pos2x = arenaConfig.getInt("pos2.x");
                int pos2y = arenaConfig.getInt("pos2.y");
                int pos2z = arenaConfig.getInt("pos2.z");

                Location pos1 = new Location(null, pos1x, pos1y, pos1z);
                Location pos2 = new Location(null, pos2x, pos2y, pos2z);

                List<Location> spawns = new ArrayList<>();
                for (String spawnCode : Objects.requireNonNull(arenaConfig.getConfigurationSection("spawns")).getKeys(false)) {
                    int spawnx = arenaConfig.getInt("spawns." + spawnCode + ".x");
                    int spawny = arenaConfig.getInt("spawns." + spawnCode + ".y");
                    int spawnz = arenaConfig.getInt("spawns." + spawnCode + ".z");
                    spawns.add(new Location(null, spawnx, spawny, spawnz));
                }

                DisasterMap map = new DisasterMap(name, worldName, pos1, pos2, spawns, icon);
                registerArena(map);
                count.getAndIncrement();
            });
            NaturalDisaster.getMainReference().getLogger().info("Carregadas " + count.get() + " arenas");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveArenas() {
        File arenaFolder = new File(mainReference.getDataFolder(), "arenas");
        for (DisasterMap map : getAllArenas()) {
            try {

                File arenaFile = new File(arenaFolder, map.getName() + ".yml");
                if (!arenaFile.exists()) {
                    FileConfiguration arenaConfig = new YamlConfiguration();
                    arenaConfig.set("name", map.getName());
                    arenaConfig.set("world", map.getWorldName());
                    arenaConfig.set("icon", map.getIcon().toString());

                    arenaConfig.set("pos1.x", map.getPos1().getBlockX());
                    arenaConfig.set("pos1.y", map.getPos1().getBlockY());
                    arenaConfig.set("pos1.z", map.getPos1().getBlockZ());

                    arenaConfig.set("pos2.x", map.getPos2().getBlockX());
                    arenaConfig.set("pos2.y", map.getPos2().getBlockY());
                    arenaConfig.set("pos2.z", map.getPos2().getBlockZ());

                    int count = 0;
                    for (Location loc : map.getSpawns()) {
                        arenaConfig.set("spawns.spawn" + count + ".x", loc.getBlockX());
                        arenaConfig.set("spawns.spawn" + count + ".y", loc.getBlockY());
                        arenaConfig.set("spawns.spawn" + count + ".z", loc.getBlockZ());
                        count++;
                    }
                    arenaConfig.save(arenaFile);
                }
            } catch (NullPointerException | IOException e) {
                NaturalDisaster.getMainReference().getLogger().log(Level.WARNING, "Erro ao salvar arena " + map.getName());
                e.printStackTrace();
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

            for (Player p : currentMap.getPlayersInArena())
                p.setInvulnerable(true);

            Bukkit.getScheduler().scheduleSyncDelayedTask(mainReference, () -> {
                currentDisaster.startDisaster();
                ActionBar ab = new ActionBar(ChatColor.RED + "Boa sorte!");
                ab.sendToAll();
                currentStatus = GameStatus.IN_GAME;
                for (Player all : currentMap.getPlayersInArena()) {
                    all.playSound(all.getLocation(), Sound.EVENT_RAID_HORN, 1f, 1f);
                    all.setInvulnerable(false);
                    PlayerUtils.healPlayer(all);
                }
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
                p.getInventory().clear();
                PlayerUtils.healPlayer(p);
            }
        }, 20L);

        // Wait 6s to unload map in order to give time to all unfinished delayed schedules
        Bukkit.getScheduler().scheduleSyncDelayedTask(mainReference, () -> {
            if (Bukkit.unloadWorld(Objects.requireNonNull(currentMap.getPos1().getWorld()), false)) {
                NaturalDisaster.getMainReference().getLogger().info("Mundo " + currentMap.getName() + " descarregado.");
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (p.isOp())
                        p.sendMessage(ChatColor.GRAY + ">>" + ChatColor.DARK_GRAY + " [DEBUG INFO] " + ChatColor.GRAY + ">> Mundo descarregado.");
                }
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

    public Disaster getCurrentDisaster() {
        return currentDisaster;
    }
}
