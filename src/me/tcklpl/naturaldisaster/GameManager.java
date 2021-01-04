package me.tcklpl.naturaldisaster;

import me.tcklpl.naturaldisaster.config.globalconfigs.GameConfig;
import me.tcklpl.naturaldisaster.disasters.Disaster;
import me.tcklpl.naturaldisaster.disasters.DisasterManager;
import me.tcklpl.naturaldisaster.exceptions.InvalidGameStartException;
import me.tcklpl.naturaldisaster.map.ArenaManager;
import me.tcklpl.naturaldisaster.map.DisasterMap;
import me.tcklpl.naturaldisaster.player.cPlayer.CPlayer;
import me.tcklpl.naturaldisaster.player.ingamePlayer.ArenaPlayerManager;
import me.tcklpl.naturaldisaster.reflection.ReflectionWorldUtils;
import me.tcklpl.naturaldisaster.util.ActionBar;
import me.tcklpl.naturaldisaster.util.NamesAndColors;
import me.tcklpl.naturaldisaster.util.PlayerUtils;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class GameManager {

    private final JavaPlugin main;
    private GameStatus currentStatus;

    private final DisasterManager disasterManager = new DisasterManager();
    private final ArenaManager arenaManager = new ArenaManager();
    private ArenaPlayerManager arenaPlayerManager;

    private Disaster currentDisaster;
    private DisasterMap currentMap;

    private final Queue<Disaster> lastDisasters = new LinkedList<>();
    private final Queue<DisasterMap> lastMaps = new LinkedList<>();

    private final Random r = new Random();

    private final GameConfig gameConfig = NaturalDisaster.getConfigManager().requestConfig(GameConfig.class);

    private int startupCounterId;

    public GameManager(JavaPlugin main) {
        this.main = main;
    }

    public boolean isIngame() {
        return currentStatus == GameStatus.IN_GAME;
    }

    public void setCurrentStatus(GameStatus currentStatus) {
        this.currentStatus = currentStatus;
    }

    public void pickNextGame() throws InvalidGameStartException {
        if (currentStatus != GameStatus.IN_LOBBY)
            throw new InvalidGameStartException();

        if (currentMap == null) {
            List<DisasterMap> arenas = arenaManager.getArenas();
            int i = r.nextInt(arenas.size());
            if (lastMaps.size() > 3)
                lastMaps.remove();
            do
                currentMap = arenas.get((i++) % arenas.size());
            while (lastMaps.contains(currentMap));
        }

        if (currentDisaster == null) {
            List<Disaster> disasters = disasterManager.getPlayableDisasters();
            int i = r.nextInt(disasters.size());
            if (lastDisasters.size() > 3)
                lastDisasters.remove();
            do
                currentDisaster = disasters.get((i++) % disasters.size());
            while (lastDisasters.contains(currentDisaster));
        }

        currentDisaster.setMap(currentMap);

        new ActionBar(ChatColor.GOLD + "Próximo mapa: " + currentMap.getName()).sendToAll();

        arenaManager.loadArenaWorld(currentMap);
    }

    public void startNextGame() {
        arenaPlayerManager = new ArenaPlayerManager(main);
        currentStatus = GameStatus.STARTING;
        currentMap.addAllPlayersToArena();

        assignRandomNames();

        AtomicInteger counter = new AtomicInteger(5);
        startupCounterId = Bukkit.getScheduler().scheduleSyncRepeatingTask(main, () -> {
            new ActionBar(ChatColor.GOLD + "Começando em: " + counter.get()).sendToAll();
            if (counter.getAndDecrement() <= 0)
                cancelStartupCounter();
        }, 20L, 20L);

        if (currentDisaster.getPrecipitationType() == ReflectionWorldUtils.Precipitation.SPECIFIC)
            currentMap.setArenaBiome(currentDisaster.getArenaSpecificBiome());
        else
            currentMap.setArenaRandomBiomeBasedOnPrecipitationType(currentDisaster.getPrecipitationType());

        currentMap.teleportPlayersToSpawns();

        for (Player p : currentMap.getPlayersInArena())
            p.setInvulnerable(true);

        Bukkit.getScheduler().scheduleSyncDelayedTask(main, () -> {
            currentDisaster.startDisaster();
            new ActionBar(ChatColor.RED + "Boa sorte!").sendToAll();
            currentStatus = GameStatus.IN_GAME;
            for (Player all : currentMap.getPlayersInArena()) {
                all.playSound(all.getLocation(), Sound.EVENT_RAID_HORN, 1f, 1f);
                all.setInvulnerable(false);
                PlayerUtils.healPlayer(all);
            }
        }, 130L);
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

    private void cancelStartupCounter() {
        Bukkit.getScheduler().cancelTask(startupCounterId);
    }

    public void registerPlayerDeath(Player p) {
        if (!currentMap.getPlayersInArena().contains(p)) return;

        arenaPlayerManager.returnPlayerToNormal(p);
        currentMap.getPlayersInArena().remove(p);

        if (currentMap.getPlayersInArena().size() <= 1) {
            Bukkit.broadcastMessage(ChatColor.GOLD + "O jogo acabou.");
            if (currentMap.getPlayersInArena().isEmpty()) {
                endGame();
            } else if (currentMap.getPlayersInArena().size() == 1) {
                Player winner = currentMap.getPlayersInArena().get(0);
                Bukkit.getScheduler().runTaskLater(main, () ->
                        Bukkit.broadcastMessage(ChatColor.GREEN + winner.getName() + " venceu!"), 20L);

                CPlayer cp = NaturalDisaster.getPlayerManager().getCPlayer(winner.getUniqueId());
                cp.getPlayerData().setWins(cp.getPlayerData().getWins() + 1);
                cp.getPlayerData().setMoney(cp.getPlayerData().getMoney() + 25);
                winner.sendMessage(ChatColor.GOLD + "+$25 por ganhar a partida.");
                endGame();
            } else {
                Bukkit.broadcastMessage(p.getDisplayName() + ChatColor.GRAY + "( " + p.getName() + " ) morreu, ainda restam " + currentMap.getPlayersInArena().size() + " jogadores vivos!");
                p.playSound(p.getLocation(), Sound.BLOCK_BEACON_DEACTIVATE, 1f, 1f);
                for (Player player : currentMap.getPlayersInArena()) {
                    player.sendMessage(ChatColor.GRAY + "+$1 por sobreviver.");

                    CPlayer cp = NaturalDisaster.getPlayerManager().getCPlayer(player.getUniqueId());
                    cp.getPlayerData().setMoney(cp.getPlayerData().getMoney() + 1);
                }
                teleportSpectatorToArena(p);
            }
        }
    }

    public void endGame() {
        currentDisaster.stopDisaster();

        for (Player p : currentMap.getPlayersInArena())
            arenaPlayerManager.returnPlayerToNormal(p);

        for (Player p : Bukkit.getOnlinePlayers()) {
            p.setDisplayName(p.getName());
            p.setPlayerListName(p.getName());
            p.setScoreboard(Objects.requireNonNull(Bukkit.getScoreboardManager()).getNewScoreboard());
        }

        NaturalDisaster.getSkinManager().applyAfterGameSkinChanges();

        Bukkit.getScheduler().scheduleSyncDelayedTask(main, () -> {
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.setGameMode(GameMode.ADVENTURE);
                p.teleport(new Location(Bukkit.getWorld("void"), 8, 8, 8));
                p.getInventory().clear();
                PlayerUtils.healPlayer(p);
            }
        }, 20L);

        Bukkit.getScheduler().scheduleSyncDelayedTask(main, () -> {
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
        }, 40L);

    }

    public void teleportSpectatorToArena(Player p) {
        if (currentStatus == GameStatus.IN_GAME) {
            p.setGameMode(GameMode.SPECTATOR);
            if (!currentMap.getPlayersInArena().isEmpty())
                p.teleport(currentMap.getPlayersInArena().get(0));
        }
    }

    public void endByTimeout() {
        for (Player p : currentMap.getPlayersInArena()) {
            CPlayer cp = NaturalDisaster.getPlayerManager().getCPlayer(p.getUniqueId());
            cp.getPlayerData().setWins(cp.getPlayerData().getWins() + 1);
            cp.getPlayerData().setMoney(cp.getPlayerData().getMoney() + 25);
            p.sendMessage(ChatColor.GOLD + "+$25 por ganhar a partida.");
        }

        Bukkit.broadcastMessage(ChatColor.YELLOW + "Acabou o tempo do mapa, todos ainda vivos ganharam");
        endGame();
    }

    //region public getters
    public GameStatus getCurrentStatus() {
        return currentStatus;
    }

    public DisasterManager getDisasterManager() {
        return disasterManager;
    }

    public ArenaManager getArenaManager() {
        return arenaManager;
    }

    public Disaster getCurrentDisaster() {
        return currentDisaster;
    }

    public DisasterMap getCurrentMap() {
        return currentMap;
    }

    public void setCurrentDisaster(Disaster currentDisaster) {
        this.currentDisaster = currentDisaster;
    }

    public void setCurrentMap(DisasterMap currentMap) {
        this.currentMap = currentMap;
    }

    //endregion

}
