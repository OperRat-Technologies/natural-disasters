package me.tcklpl.naturaldisaster.util;

import me.tcklpl.naturaldisaster.reflection.Packets;
import me.tcklpl.naturaldisaster.reflection.ReflectionUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class ActionBar {

    private Object packet;

    public ActionBar(String text) {
        this.packet = Packets.Play.PlayOutChat("{\"text\":\"" + text + "\"}", Packets.Play.ChatMessageType.GAME_INFO);
    }

    public void sendToPlayer(Player p) {
        ReflectionUtils.sendPacket(p, packet);
    }

    public void sendToAll() {
        for (Player p : Bukkit.getOnlinePlayers())
            sendToPlayer(p);
    }
}
