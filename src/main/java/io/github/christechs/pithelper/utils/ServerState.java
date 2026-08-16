package io.github.christechs.pithelper.utils;

import java.util.regex.Pattern;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import io.github.christechs.pithelper.compat.McCompat;
import io.github.christechs.pithelper.data.HypixelGameMode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;

public class ServerState {
	private static final Pattern STRIP_COLOR = Pattern.compile("(?i)§[0-9A-FK-OR]");
	public static boolean onHypixel = false;
	public static HypixelGameMode currentGameMode = HypixelGameMode.UNKNOWN;
	private static int locrawTicks = 0;

	public static void onJoin() {
		onHypixel = McCompat.isHypixelAddress();
		currentGameMode = HypixelGameMode.UNKNOWN;
		locrawTicks = onHypixel ? 40 : 0;
	}

	public static void onDisconnect() {
		onHypixel = false;
		currentGameMode = HypixelGameMode.UNKNOWN;
		locrawTicks = 0;
	}

	public static void updateFromPacket(String serverType, String lobbyName) {
		onHypixel = true;
		currentGameMode = HypixelGameMode.fromPacket(serverType == null ? "" : serverType, lobbyName == null ? "" : lobbyName);
	}

	public static void tick(Minecraft client) {
		if (client.level == null || client.player == null) {
			return;
		}
		if (!onHypixel) {
			onHypixel = McCompat.isHypixelAddress();
		}
		if (locrawTicks > 0) {
			locrawTicks--;
			if (locrawTicks == 0 && onHypixel) {
				client.player.connection.sendChat("/locraw");
			}
		}
		if (onHypixel) {
			HypixelGameMode fromScoreboard = fromScoreboard(client.level);
			if (fromScoreboard != HypixelGameMode.UNKNOWN) {
				currentGameMode = fromScoreboard;
			} else if (currentGameMode == HypixelGameMode.UNKNOWN) {
				currentGameMode = HypixelGameMode.MAIN;
			}
		}
	}

	public static boolean tryConsumeLocraw(String message) {
		String trim = STRIP_COLOR.matcher(message).replaceAll("").trim();
		if (!trim.startsWith("{") || !trim.contains("\"server\"")) {
			return false;
		}
		try {
			JsonObject obj = JsonParser.parseString(trim).getAsJsonObject();
			if (!obj.has("server") || (!obj.has("gametype") && !obj.has("lobbyname") && !obj.has("mode"))) {
				return false;
			}
			String gametype = obj.has("gametype") ? obj.get("gametype").getAsString() : "";
			String lobby = obj.has("lobbyname") ? obj.get("lobbyname").getAsString()
				: obj.has("mode") ? obj.get("mode").getAsString() : "";
			updateFromPacket(gametype, lobby);
			return true;
		} catch (Exception ignored) {
			return false;
		}
	}

	private static HypixelGameMode fromScoreboard(ClientLevel level) {
		Objective objective = level.getScoreboard().getDisplayObjective(DisplaySlot.SIDEBAR);
		if (objective == null) {
			return HypixelGameMode.UNKNOWN;
		}
		String title = STRIP_COLOR.matcher(objective.getDisplayName().getString().toLowerCase()).replaceAll("");
		for (HypixelGameMode mode : HypixelGameMode.values()) {
			if (mode == HypixelGameMode.UNKNOWN || mode.fallbackLobbyPrefix.isEmpty()) {
				continue;
			}
			if (title.contains(mode.fallbackLobbyPrefix) || title.contains(mode.niceName.toLowerCase())) {
				return mode;
			}
		}
		if (title.contains("pit")) {
			return HypixelGameMode.PIT;
		}
		return HypixelGameMode.UNKNOWN;
	}
}
