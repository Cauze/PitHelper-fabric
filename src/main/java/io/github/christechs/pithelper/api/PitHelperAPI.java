/*
 * PitHelper - A Hypixel The Pit Helper Mod.
 * Copyright (C) 2026 Christian Steenkamp
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package io.github.christechs.pithelper.api;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.Minecraft;
import net.minecraft.client.User;

import java.io.InputStreamReader;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

public class PitHelperAPI {

    private static final String BASE_URL = "https://api.pithelper.org";
    private static final String USER_AGENT = "PitHelper-Mod";

    public static int lastLimit = 0;
    public static int lastRemaining = 0;
    public static int lastResetSec = 0;

    public static CompletableFuture<String> generateNewKey() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                User user = Minecraft.getInstance().getUser();
                String salt = generateSalt();
                String uuid = user.getProfileId().toString();
                String hash = generateHash(uuid, salt);

                joinMojangServer(user, hash);
                return registerWithApiServer(user.getName(), hash);
            } catch (Exception e) {
                throw new RuntimeException("Key Error: " + e.getMessage(), e);
            }
        });
    }

    public static CompletableFuture<JsonObject> getPlayerMain(String apiKey, String nameOrUuid) {
        return fetchPlayerEndpoint(apiKey, nameOrUuid, "main");
    }

    public static CompletableFuture<JsonObject> getPlayerStats(String apiKey, String nameOrUuid) {
        return fetchPlayerEndpoint(apiKey, nameOrUuid, "stats");
    }

    public static CompletableFuture<JsonObject> getPlayerProfile(String apiKey, String nameOrUuid) {
        return fetchPlayerEndpoint(apiKey, nameOrUuid, "profile");
    }

    public static CompletableFuture<JsonObject> getPlayerInventories(String apiKey, String nameOrUuid) {
        return fetchPlayerEndpoint(apiKey, nameOrUuid, "inventories");
    }

    public static CompletableFuture<JsonObject> searchItems(String apiKey, String enchant, int minLevel, int minTier) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String endpoint = String.format("/items/search?enchant=%s&min_level=%d&min_tier=%d", enchant, minLevel, minTier);
                return fetchWithPolling(apiKey, endpoint, 1);
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        });
    }

    private static CompletableFuture<JsonObject> fetchPlayerEndpoint(String apiKey, String nameOrUuid, String endpoint) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String uuid = resolveUuid(nameOrUuid);
                return fetchWithPolling(apiKey, "/player/" + uuid + "/" + endpoint, 15);
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        });
    }

    private static void joinMojangServer(User user, String serverHash) throws Exception {
        HttpURLConnection conn = (HttpURLConnection) new URL("https://sessionserver.mojang.com/session/minecraft/join").openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");

        JsonObject body = new JsonObject();
        body.addProperty("accessToken", user.getAccessToken());
        body.addProperty("selectedProfile", user.getProfileId().toString().replace("-", ""));
        body.addProperty("serverId", serverHash);

        byte[] bodyBytes = body.toString().getBytes(StandardCharsets.UTF_8);
        conn.setRequestProperty("Content-Length", String.valueOf(bodyBytes.length));
        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(bodyBytes);
        }
        if (conn.getResponseCode() != 204) throw new RuntimeException("Mojang authentication failed.");
    }

    private static String registerWithApiServer(String username, String serverHash) throws Exception {
        HttpURLConnection conn = (HttpURLConnection) new URL(BASE_URL + "/auth/register").openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("User-Agent", USER_AGENT);
        conn.setDoOutput(true);

        JsonObject body = new JsonObject();
        body.addProperty("username", username);
        body.addProperty("hash", serverHash);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(body.toString().getBytes(StandardCharsets.UTF_8));
        }

        JsonObject response = parseResponse(conn);
        if (!response.get("success").getAsBoolean()) throw new RuntimeException("Failed to register key.");
        return response.get("token").getAsString();
    }

    private static JsonObject fetchWithPolling(String apiKey, String endpoint, int maxRetries) throws Exception {
        for (int i = 0; i < maxRetries; i++) {
            HttpURLConnection conn = (HttpURLConnection) new URL(BASE_URL + endpoint).openConnection();
            if (apiKey != null && !apiKey.trim().isEmpty()) conn.setRequestProperty("X-API-Key", apiKey);
            conn.setRequestProperty("User-Agent", USER_AGENT);

            int status = conn.getResponseCode();

            if (conn.getHeaderField("X-RateLimit-Limit") != null) {
                lastLimit = Integer.parseInt(conn.getHeaderField("X-RateLimit-Limit"));
                lastRemaining = Integer.parseInt(conn.getHeaderField("X-RateLimit-Remaining"));
                lastResetSec = Integer.parseInt(conn.getHeaderField("X-RateLimit-Reset"));
            }

            JsonObject response = parseResponse(conn);

            if (status == 200) return response;
            if (status == 202) Thread.sleep(2000);
            else if (status == 429) {
                int retry = response.has("retry_after_seconds") ? response.get("retry_after_seconds").getAsInt() : 60;
                throw new RuntimeException("Rate Limited! Reset in " + retry + "s");
            } else {
                throw new RuntimeException(response.has("error") ? response.get("error").getAsString() : "HTTP " + status);
            }
        }
        throw new RuntimeException("Queue timed out. Try again later.");
    }

    public static String resolveUuid(String nameOrUuid) throws Exception {
        if (nameOrUuid.length() > 16) return nameOrUuid.replace("-", "");

        try {
            HttpURLConnection conn = (HttpURLConnection) new URL("https://api.mojang.com/users/profiles/minecraft/" + nameOrUuid).openConnection();
            conn.setConnectTimeout(3000);
            conn.setReadTimeout(3000);
            if (conn.getResponseCode() == 200) {
                InputStreamReader reader = new InputStreamReader(conn.getInputStream());
                JsonObject response = new JsonParser().parse(reader).getAsJsonObject();
                reader.close();
                return response.get("id").getAsString();
            }
        } catch (Exception ignored) {
        }

        HttpURLConnection fallbackConn = (HttpURLConnection) new URL("https://api.ashcon.app/mojang/v2/user/" + nameOrUuid).openConnection();
        fallbackConn.setConnectTimeout(5000);
        fallbackConn.setReadTimeout(5000);
        if (fallbackConn.getResponseCode() == 200) {
            InputStreamReader reader = new InputStreamReader(fallbackConn.getInputStream());
            JsonObject response = new JsonParser().parse(reader).getAsJsonObject();
            reader.close();
            return response.get("uuid").getAsString().replace("-", "");
        }

        throw new RuntimeException("Minecraft player not found or all APIs are rate-limited.");
    }

    private static JsonObject parseResponse(HttpURLConnection conn) throws Exception {
        boolean isError = conn.getResponseCode() >= 400;
        try (InputStreamReader reader = new InputStreamReader(isError ? conn.getErrorStream() : conn.getInputStream(), StandardCharsets.UTF_8)) {
            return new JsonParser().parse(reader).getAsJsonObject();
        } finally {
            conn.disconnect();
        }
    }

    private static String generateSalt() {
        return new BigInteger(200, new Random()).toString(32).substring(0, 40);
    }

    private static String generateHash(String uuid, String salt) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        return new BigInteger(md.digest(("pithelper_" + uuid.replace("-", "") + salt).getBytes(StandardCharsets.UTF_8))).toString(16);
    }
}