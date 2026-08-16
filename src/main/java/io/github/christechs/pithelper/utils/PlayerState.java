package io.github.christechs.pithelper.utils;

import java.util.regex.Pattern;

import io.github.christechs.pithelper.config.PitConfig;
import io.github.christechs.pithelper.data.HypixelGameMode;

public class PlayerState {
	private static final Pattern STRIP_COLOR_PATTERN = Pattern.compile("(?i)§[0-9A-FK-OR]");
	public static long lastDeath = 0;

	public void onChat(String message) {
		if (PitConfig.general().onlyOnHypixel && (!ServerState.onHypixel || ServerState.currentGameMode != HypixelGameMode.PIT)) {
			return;
		}
		String clean = STRIP_COLOR_PATTERN.matcher(message.toLowerCase()).replaceAll("");
		if (clean.startsWith("death! by") && clean.endsWith("view recap")) {
			PlayerState.lastDeath = System.currentTimeMillis();
		}
	}
}
