package me.tcklpl.naturaldisaster.util;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import me.tcklpl.naturaldisaster.NaturalDisaster;
import me.tcklpl.naturaldisaster.player.skins.CustomSkin;
import me.tcklpl.naturaldisaster.reflection.Packets;
import me.tcklpl.naturaldisaster.reflection.ReflectionUtils;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.JSONArray;
import org.json.JSONObject;

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
import java.util.stream.Collectors;

public class SkinUtils {

    private static String entityHumanGameProfileFieldName = null;

    /**
     * Requests the original UUID from the Mojang API regarding the informed player.
     * @param player the player to get the original UUID.
     * @return the original UUID if the requested name has an account, otherwise null.
     */
    public static String getOriginalUUIDString(Player player) {
        try {
            HttpsURLConnection premiumUUID = (HttpsURLConnection) new URL(String.format("https://api.mojang.com/users/profiles/minecraft/%s", player.getName())).openConnection();
            if (premiumUUID.getResponseCode() == HttpURLConnection.HTTP_OK) {

                // Line readed will be as follows:
                // was: {"id":"<PLAYER UUID>","name":"<PLAYER NAME>"}
                // now is: {"name":"<PLAYER NAME>", "id":"<PLAYER UUID>"}
                String rep = new BufferedReader(new InputStreamReader(premiumUUID.getInputStream())).readLine();
                JSONObject json = new JSONObject(rep);
                return json.getString("id");
            } else return null;
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Modifies the GameProfile attribute inside the requested Player using reflection.
     * @param entityPlayer the entityPlayer instance that will be changed, p.getHandle().
     * @param gameProfile the GameProfile that will replace the one present in the entityPlayer above.
     */
    public static void setGameProfile(Object entityPlayer, GameProfile gameProfile) {
        try {
            // As the code is obfuscated, this gets the EntityHuman field from the superclass regardless of the field name.
            if (entityHumanGameProfileFieldName == null) {
                // EntityPlayer's superclass is EntityHuman.
                Field[] fields = entityPlayer.getClass().getSuperclass().getDeclaredFields();
                for (Field f : fields) {
                    if (f.getType().equals(GameProfile.class)) {
                        entityHumanGameProfileFieldName = f.getName();
                    }
                }
                if (entityHumanGameProfileFieldName == null)
                    throw new NoSuchFieldException("Unable to find GameProfile attribute");
            }
            Field gp = entityPlayer.getClass().getSuperclass().getDeclaredField(entityHumanGameProfileFieldName);

            gp.setAccessible(true);
            gp.set(entityPlayer, gameProfile);
            gp.setAccessible(false);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * Requests the player's skin from Mojang, using the original player's UUID.
     * @param uuidString the player's original UUID.
     * @return a new instance of CustomSkin from the gathered values.
     */
    public static CustomSkin getSkinFromMojang(String uuidString) {
        try {
            HttpsURLConnection connection = (HttpsURLConnection) new URL(
                    String.format("https://sessionserver.mojang.com/session/minecraft/profile/%s?unsigned=false", uuidString)).openConnection();

            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                // Reply format as follows:
                // {"id":"<PLAYER UUID>","name":"<PLAYER NAME>","properties":[{"name":"textures","value":"<TEXTURE VALUE>","signature":"<MOJANG SIGNATURE>"}]}
                String reply = new BufferedReader(new InputStreamReader(connection.getInputStream())).lines().collect(Collectors.joining());
                JSONObject json = new JSONObject(reply);
                JSONArray properties = json.getJSONArray("properties");
                JSONObject textures = properties.getJSONObject(0);

                String playerName = json.getString("name");
                String textureValue = textures.getString("value");
                String signature = textures.getString("signature");

                return new CustomSkin(playerName, textureValue, signature, new Timestamp(System.currentTimeMillis()));

            } else {
                NaturalDisaster.getMainReference().getLogger().warning("Response NOT OK:");
                NaturalDisaster.getMainReference().getLogger().warning(connection.getResponseMessage());
                return null;
            }

        } catch (IOException e) {
            NaturalDisaster.getMainReference().getLogger().warning("Exception");
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Applies the desired CustomSkin on the Player and updates the player for everyone online.
     * @param main a reference to JavaPlugin.
     * @param p the Player to change the skin.
     * @param skin the new CustomSkin.
     */
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
