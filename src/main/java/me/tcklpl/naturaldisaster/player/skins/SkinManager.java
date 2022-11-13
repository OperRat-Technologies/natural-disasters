package me.tcklpl.naturaldisaster.player.skins;

import me.tcklpl.naturaldisaster.GameStatus;
import me.tcklpl.naturaldisaster.NaturalDisaster;
import me.tcklpl.naturaldisaster.reflection.ReflectionUtils;
import me.tcklpl.naturaldisaster.util.SkinUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.*;

public class SkinManager {

    private static class QueuedPlayer {

        private final Player player;
        private final String uuidStr;
        private int attempts;

        public QueuedPlayer(Player player, String uuidStr) {
            this.player = player;
            this.uuidStr = uuidStr;
            this.attempts = 0;
        }

        public Player getPlayer() {
            return player;
        }

        public String getUuidStr() {
            return uuidStr;
        }

        public boolean canAttemptAgain() {
            return ++attempts <= 3;
        }
    }
    
    private final FileConfiguration skinsConfig;
    private final File skinsFile;
    private final JavaPlugin main;
    private final List<CustomSkin> managedSkins;
    private final Queue<QueuedPlayer> skinApplyQueue;
    private int queueTaskId;
    private final HashMap<Player, CustomSkin> skinsToApplyAfterGame;

    public SkinManager(JavaPlugin main) {
        managedSkins = new ArrayList<>();
        skinApplyQueue = new LinkedList<>();
        skinsToApplyAfterGame = new HashMap<>();
        queueTaskId = 0;
        this.main = main;
        skinsFile = new File(main.getDataFolder(), "skins.yml");
        if (skinsFile.exists())
            skinsConfig = YamlConfiguration.loadConfiguration(skinsFile);
        else skinsConfig = new YamlConfiguration();
    }

    public void setupSkins() {
        if (skinsFile.exists()) {
            if (skinsConfig.getConfigurationSection("skins") != null) {
                for (String skinName : Objects.requireNonNull(skinsConfig.getConfigurationSection("skins")).getKeys(false)) {
                    String value = skinsConfig.getString("skins." + skinName + ".value");
                    String signature = skinsConfig.getString("skins." + skinName + ".signature");
                    String timestamp = skinsConfig.getString("skins." + skinName + ".timestamp");
                    assert timestamp != null;
                    managedSkins.add(new CustomSkin(skinName, value, signature, Timestamp.valueOf(timestamp)));
                }
                NaturalDisaster.getMainReference().getLogger().info("Carregadas " + managedSkins.size() + " skins para lobby");
            }
        }
    }

    public void saveSkins() throws IOException {
        for (CustomSkin cs : managedSkins) {
            skinsConfig.set("skins." + cs.getName() + ".value", cs.getValue());
            skinsConfig.set("skins." + cs.getName() + ".signature", cs.getSignature());
            skinsConfig.set("skins." + cs.getName() + ".timestamp", cs.getTimestamp().toString());
        }
        skinsConfig.save(skinsFile);
    }

    public boolean isRegistered(String skinName) {
        for (CustomSkin cs : managedSkins) {
            if (cs.getName().equalsIgnoreCase(skinName))
                return true;
        }
        return false;
    }

    public CustomSkin getSkin(String name) {
        for (CustomSkin cs : managedSkins) {
            if (cs.getName().equalsIgnoreCase(name))
                return cs;
        }
        return null;
    }

    public void addPlayerToSkinQueue(Player p, String uuidStr) {
        skinApplyQueue.add(new QueuedPlayer(p, uuidStr));
        p.sendMessage(ChatColor.GRAY + "Você foi adicionado na fila de download de skin, isso pode demorar alguns minutos dependendo do número de pedidos");
        if (queueTaskId == 0) {
            queueTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(main, () -> {
                if (skinApplyQueue.size() == 0 || skinApplyQueue.peek() == null)
                    killQueueTask();
                QueuedPlayer qp = skinApplyQueue.remove();
                CustomSkin skin = SkinUtils.getSkinFromMojang(qp.getUuidStr());
                if (skin != null) {
                    boolean online = false;
                    for (Player all : Bukkit.getOnlinePlayers()) {
                        if (all.getUniqueId().equals(qp.getPlayer().getUniqueId()))
                            online = true;
                    }
                    if (online) {
                        Player target = Bukkit.getPlayer(qp.getPlayer().getUniqueId());
                        assert target != null;
                        if (NaturalDisaster.getGameManager().getCurrentStatus() != GameStatus.IN_LOBBY) {
                            skinsToApplyAfterGame.put(target, skin);
                            target.sendMessage(ChatColor.GRAY + "Sua skin foi obtida, ela será aplicada assim que a partida acabar e você voltar ao lobby");
                        } else {
                            SkinUtils.applySkin(main, target, skin);
                            ReflectionUtils.updatePlayerForEveryone(main, target);
                            target.sendMessage(ChatColor.GRAY + "Sua skin foi obtida, caso ela não tenha sido aplicada relogue do servidor ou aguarde o fim da próxima partida.");
                        }
                    }
                    managedSkins.remove(skin);
                    managedSkins.add(skin);
                } else {
                    if (qp.canAttemptAgain()) {
                        qp.getPlayer().sendMessage(ChatColor.RED + "Falha ao adquirir sua skin dos servers da mojang, você foi colocado na lista novamente");
                        skinApplyQueue.add(qp);
                    } else {
                        qp.getPlayer().sendMessage(ChatColor.RED + "Falha ao adquirir sua skin dos servers da mojang, você excedeu o número de tentativas.");
                    }
                }
            }, 0L, 3900L);
            // Task will repeat each 65 seconds
        }
    }

    public void applyAfterGameSkinChanges() {
        if (skinsToApplyAfterGame.size() > 0) {
            for (Player p : skinsToApplyAfterGame.keySet()) {
                SkinUtils.applySkin(main, p, skinsToApplyAfterGame.get(p));
                ReflectionUtils.updatePlayerForEveryone(main, p);
                p.sendMessage(ChatColor.GRAY + "Sua skin foi aplicada. Caso não a veja relogue do servidor ou espere o final da próxima partida.");
            }
            skinsToApplyAfterGame.clear();
        }
    }

    private void killQueueTask() {
        if (queueTaskId != 0) {
            Bukkit.getScheduler().cancelTask(queueTaskId);
            queueTaskId = 0;
        }
    }


}
