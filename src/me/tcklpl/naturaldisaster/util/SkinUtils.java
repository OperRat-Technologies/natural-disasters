package me.tcklpl.naturaldisaster.util;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import me.tcklpl.naturaldisaster.NaturalDisaster;
import me.tcklpl.naturaldisaster.player.skins.CustomSkin;
import me.tcklpl.naturaldisaster.reflection.Packets;
import me.tcklpl.naturaldisaster.reflection.ReflectionUtils;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Timestamp;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class SkinUtils {

    public static String getOriginalUUIDString(String playerName) {
        try {
            HttpsURLConnection premiumUUID = (HttpsURLConnection) new URL(String.format("https://api.mojang.com/users/profiles/minecraft/%s", playerName)).openConnection();
            if (premiumUUID.getResponseCode() == HttpURLConnection.HTTP_OK) {

                // Line readed will be as follows:
                // was: {"id":"<PLAYER UUID>","name":"<PLAYER NAME>"}
                // now is: {"name":"<PLAYER NAME>", "id":"<PLAYER UUID>"}
                String rep = new BufferedReader(new InputStreamReader(premiumUUID.getInputStream())).readLine();

                // Gets only the UUID from the server response
                return rep.split("\"id\":\"")[1].split("\"")[0];

            } else return null;
        } catch (IOException e) {
            return null;
        }
    }

    public static UUID getUndashedStringAsUUID(String input) {
        return UUID.fromString(input.replaceFirst("(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)", "$1-$2-$3-$4-$5"));
    }

    public static void setGameProfile(Object entityLiving, GameProfile gameProfile) {
        try {
            // Game profile na 1.15 era "bT"
            // Game profile na 1.16 é   "bQ"
            Field gp2 = entityLiving.getClass().getSuperclass().getDeclaredField("bQ");
            gp2.setAccessible(true);
            gp2.set(entityLiving, gameProfile);
            gp2.setAccessible(false);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public static CustomSkin getSkinFromMojang(String uuidString) {

        try {

            HttpsURLConnection connection = (HttpsURLConnection) new URL(String.format("https://sessionserver.mojang.com/session/minecraft/profile/%s?unsigned=false", uuidString)).openConnection();

            NaturalDisaster.getMainReference().getLogger().info(connection.getURL().toString());

            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {

                // Reply format as follows:
                // {"id":"<PLAYER UUID>","name":"<PLAYER NAME>","properties":[{"name":"textures","value":"<TEXTURE VALUE>","signature":"<MOJANG SIGNATURE>"}]}
                String reply = new BufferedReader(new InputStreamReader(connection.getInputStream())).lines().collect(Collectors.joining());

                NaturalDisaster.getMainReference().getLogger().info(reply);

                String playerName = reply.split("\"name\" : \"")[1].split("\"")[0];
                String textureValue = reply.split("\"name\" : \"textures\", {4}\"value\" : \"")[1].split("\"")[0];
                String signature = reply.split("\"signature\" : \"")[1].split("\"")[0];

                NaturalDisaster.getMainReference().getLogger().info(playerName + "|");
                NaturalDisaster.getMainReference().getLogger().info(textureValue + "|");
                NaturalDisaster.getMainReference().getLogger().info(signature + "|");

//                Pattern p = Pattern.compile("\"name\" : \"(\\w+)\",\\n.+\\n.+\\n.+\"value\" : \"(.+)\",\\n.+\"signature\" : \"(.+)\"");
//                Matcher m = p.matcher(reply);
//
//                NaturalDisaster.getMainReference().getLogger().info(p.toString());
//                NaturalDisaster.getMainReference().getLogger().info(m.toString());
//
//                if (m.find()) {
//
//                    String playerName = m.group(1);
//                    String textureValue = m.group(2);
//                    String signature = m.group(3);
//
//                }

                return new CustomSkin(playerName, textureValue, signature, new Timestamp(System.currentTimeMillis()));

            } else {
                NaturalDisaster.getMainReference().getLogger().warning("Response NOT ok:");
                NaturalDisaster.getMainReference().getLogger().warning(connection.getResponseMessage());
                return null;
            }

        } catch (IOException e) {
            NaturalDisaster.getMainReference().getLogger().warning("Exception");
            e.printStackTrace();
        }
        return null;
    }

    public static void applySkin(JavaPlugin main, Player p, CustomSkin skin) {

        try {

            Object playerHandle = p.getClass().getMethod("getHandle").invoke(p); // EntityPlayer

            GameProfile gameProfile = (GameProfile) p.getClass().getMethod("getProfile").invoke(p);
            gameProfile.getProperties().put("textures", new Property("textures", skin.getValue(), skin.getSignature()));

            Object packetPlayerOutRemove = Packets.Play.PlayOutPlayerInfo(Packets.Play.PlayerInfoEnum.REMOVE_PLAYER, p);
            ReflectionUtils.sendPacket(p, packetPlayerOutRemove);

            setGameProfile(playerHandle, gameProfile);

            Object packetPlayerOutAdd = Packets.Play.PlayOutPlayerInfo(Packets.Play.PlayerInfoEnum.ADD_PLAYER, p);
            ReflectionUtils.sendPacket(p, packetPlayerOutAdd);

            ReflectionUtils.updatePlayerForEveryone(main, p);

        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

}
