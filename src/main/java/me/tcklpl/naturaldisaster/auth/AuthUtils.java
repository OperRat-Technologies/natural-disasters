package me.tcklpl.naturaldisaster.auth;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class AuthUtils {

    public static class PremiumCheck {

        private JSONObject response;

        public PremiumCheck(String playerName, String serverHash) throws IOException {
            HttpsURLConnection connection = (HttpsURLConnection) new URL(
                    String.format("https://sessionserver.mojang.com/session/minecraft/hasJoined?username=%s&serverId=%s", playerName, serverHash)).openConnection();
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                String reply = new BufferedReader(new InputStreamReader(connection.getInputStream())).readLine();
                response = (JSONObject) JSONValue.parse(reply);
            }
        }

        public boolean isPremium() {
            return response != null;
        }

        public JSONObject getResponse() { return response; }
    }

}
