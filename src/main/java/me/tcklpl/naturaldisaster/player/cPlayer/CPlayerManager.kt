package me.tcklpl.naturaldisaster.player.cPlayer;

import me.tcklpl.naturaldisaster.NaturalDisaster;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

public class CPlayerManager {

    private final List<CPlayer> managedPlayers;

    public CPlayerManager() {
        managedPlayers = new ArrayList<>();
    }

    public CPlayer getCPlayer(UUID uuid) {
        for (CPlayer cp : managedPlayers)
            if (cp.getUuid().equals(uuid))
                return cp;
        return null;
    }

    public CPlayer getCPlayer(String name) {
        for (CPlayer cp : managedPlayers)
            if (cp.getPlayerData().getName().equals(name))
                return cp;
        return null;
    }

    public boolean registerCPlayer(CPlayer cp) {
        return managedPlayers.add(cp);
    }

    public void loadPlayers() {
        File playerFolder = new File(NaturalDisaster.getMainReference().getDataFolder() + "/players");
        if (playerFolder.exists() && playerFolder.isDirectory()) {

            try (Stream<Path> walk = Files.walk(Paths.get(NaturalDisaster.getMainReference().getDataFolder() + "/players"))) {

                List<String> result = walk.map(Path::toString).filter(f -> f.endsWith(".player")).toList();
                for (String playerFileName : result) {
                    File playerFile = new File(playerFileName);

                    FileInputStream fileInputStream = new FileInputStream(playerFile);
                    ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);

                    CPlayer cp = (CPlayer) objectInputStream.readObject();
                    objectInputStream.close();

                    managedPlayers.add(cp);
                }

                NaturalDisaster.getMainReference().getLogger().info("Carregados " + result.size() + " jogadores");

            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    public void savePlayers() {
        if (!managedPlayers.isEmpty()) {
            File playerFolder = new File(NaturalDisaster.getMainReference().getDataFolder() + "/players");
            if (!(playerFolder.exists() && playerFolder.isDirectory())) {
                if (!playerFolder.mkdirs()) {
                    NaturalDisaster.getMainReference().getLogger().warning("Falha ao criar diretório para players");
                    return;
                }
            }
            int count = 0;
            for (CPlayer cp : managedPlayers) {
                if (cp.getPlayerData().isModified()) {
                    File saveFile = new File(NaturalDisaster.getMainReference().getDataFolder() + "/players", cp.getPlayerData().getName() + ".player");
                    if (saveFile.exists())
                        if (!saveFile.delete()) {
                            NaturalDisaster.getMainReference().getLogger().severe("NÃO FOI POSSÍVEL EXCLUIR ARQUIVO DO JOGADOR " + cp.getPlayerData().getName());
                            return;
                        }

                    FileOutputStream fileOutputStream;
                    ObjectOutputStream objectOutputStream;

                    try {
                        fileOutputStream = new FileOutputStream(saveFile);
                        objectOutputStream = new ObjectOutputStream(fileOutputStream);
                        objectOutputStream.writeObject(cp);
                        objectOutputStream.close();
                        count++;
                    } catch (IOException e) {
                        NaturalDisaster.getMainReference().getLogger().warning("ERRO AO SALVAR JOGADOR " + cp.getPlayerData().getName());
                        e.printStackTrace();
                    }
                }
            }
            NaturalDisaster.getMainReference().getLogger().info("Salvos " + count + " jogadores");
        }
    }
}
