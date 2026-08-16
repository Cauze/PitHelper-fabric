package io.github.christechs.pithelper.features;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.github.christechs.pithelper.PitHelper;
import io.github.christechs.pithelper.compat.McCompat;
import io.github.christechs.pithelper.config.PitConfig;
import io.github.christechs.pithelper.data.HypixelGameMode;
import io.github.christechs.pithelper.utils.PlayerState;
import io.github.christechs.pithelper.utils.ServerState;
import net.minecraft.client.Minecraft;

public class AutoSpawnHandler {
	private static final Pattern COMBAT_PATTERN = Pattern.compile("(?i)hold up!.*\\((\\d+)s.*\\)");
	private boolean isActive = false;
	private boolean pendingRetry = false;
	private long nextExecutionTime = 0;
	private long activatedAt = 0;

	public void onTick(Minecraft mc) {
		if (mc.player == null) {
			return;
		}
		while (PitHelper.autoSpawnKey != null && PitHelper.autoSpawnKey.consumeClick()) {
			toggle(mc);
		}
		double distanceXZ = Math.hypot(mc.player.getX(), mc.player.getZ());
		if (mc.player.getY() > 100.0 && distanceXZ < 30.0) {
			if (isActive || pendingRetry) {
				isActive = false;
				pendingRetry = false;
				McCompat.chat("§a§l[PitHelper] §eSafe spawn area detected. Auto Spawn deactivated.");
			}
			return;
		}
		if (!isActive) {
			return;
		}
		if (!PitConfig.general().autoSpawnEnabled || !ServerState.onHypixel || ServerState.currentGameMode != HypixelGameMode.PIT) {
			isActive = false;
			return;
		}
		if (PlayerState.lastDeath > activatedAt) {
			isActive = false;
			pendingRetry = false;
			McCompat.chat("§c§l[PitHelper] §eYou died! Auto Spawn queue cancelled.");
			return;
		}
		if (pendingRetry && System.currentTimeMillis() >= nextExecutionTime) {
			pendingRetry = false;
			mc.player.connection.sendChat("/spawn");
		}
	}

	private void toggle(Minecraft mc) {
		if (!PitConfig.general().autoSpawnEnabled) {
			return;
		}
		if (!ServerState.onHypixel || ServerState.currentGameMode != HypixelGameMode.PIT) {
			return;
		}
		isActive = !isActive;
		if (isActive) {
			activatedAt = System.currentTimeMillis();
			pendingRetry = false;
			McCompat.chat("§a§l[PitHelper] §eAuto Spawn activated! Attempting /spawn...");
			mc.player.connection.sendChat("/spawn");
		} else {
			McCompat.chat("§c§l[PitHelper] §eAuto Spawn cancelled.");
		}
	}

	public void onChat(String raw) {
		if (!isActive) {
			return;
		}
		String clean = raw.replaceAll("(?i)§[0-9A-FK-OR]", "").trim();
		Matcher m = COMBAT_PATTERN.matcher(clean);
		if (m.find()) {
			int seconds = Integer.parseInt(m.group(1));
			long delay = seconds <= 0 ? 500 : (seconds * 1000L) + 100;
			nextExecutionTime = System.currentTimeMillis() + delay;
			pendingRetry = true;
		} else if (clean.equalsIgnoreCase("Respawning...") || clean.contains("Teleporting to spawn")) {
			isActive = false;
			pendingRetry = false;
			McCompat.chat("§a§l[PitHelper] §eSpawn successful. Auto Spawn deactivated.");
		}
	}
}
