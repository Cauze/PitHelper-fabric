package io.github.christechs.pithelper.features;

import java.util.HashSet;
import java.util.Set;

import io.github.christechs.pithelper.compat.McCompat;
import io.github.christechs.pithelper.config.PitConfig;
import io.github.christechs.pithelper.data.EventFetcher;
import io.github.christechs.pithelper.data.PitEvent;
import io.github.christechs.pithelper.utils.ServerState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;

public class NotificationHandler {
	private static final Set<String> notifiedEvents = new HashSet<>();
	private static final Set<String> countdownTicks = new HashSet<>();
	public static Screen pendingScreen = null;
	private static long lastPurgeTime = 0;

	public void onTick(Minecraft mc) {
		if (pendingScreen != null) {
			mc.setScreen(pendingScreen);
			pendingScreen = null;
		}

		NotificationManager.tick();
		EventFetcher.updateState();

		long currentTime = System.currentTimeMillis();
		if (currentTime - lastPurgeTime > 10000) {
			notifiedEvents.removeIf(key -> {
				try {
					long time = Long.parseLong(key.replaceAll("[^0-9]", ""));
					return currentTime > time + 60000;
				} catch (Exception e) {
					return true;
				}
			});
			countdownTicks.removeIf(key -> !notifiedEvents.contains(key.split("_sec_")[0]));
			lastPurgeTime = currentTime;
		}

		if (mc.player == null || EventFetcher.cachedEvents == null) {
			return;
		}
		if (PitConfig.general().onlyOnHypixel && !ServerState.onHypixel) {
			return;
		}
		if (ServerState.onHypixel && ServerState.currentGameMode != null) {
			Boolean toggle = PitConfig.gamemodes().toggles.get(ServerState.currentGameMode);
			if (toggle != null && !toggle) {
				return;
			}
		}

		for (PitEvent e : EventFetcher.cachedEvents) {
			long actualStart = e.timestamp + e.eventType.startOffset;
			if (actualStart < currentTime) {
				continue;
			}
			long timeDiff = actualStart - currentTime;
			String eventKey = e.event + actualStart;
			PitConfig.EventData settings = PitConfig.events().settings.get(e.eventType);
			if (settings != null && settings.enabled) {
				long targetNotifyTime = (settings.notifyMinutes * 60L + settings.notifySeconds) * 1000L;
				if (timeDiff <= targetNotifyTime && !notifiedEvents.contains(eventKey)) {
					notifiedEvents.add(eventKey);
					long totalSecondsRemaining = timeDiff / 1000L;
					if (PitConfig.general().chatNotifications) {
						McCompat.chat("§6§l[PitHelper] §e" + e.event + " §7starts in §a" + (totalSecondsRemaining / 60) + "m " + (totalSecondsRemaining % 60) + "s§7!");
					}
					if (PitConfig.general().visualNotifications) {
						NotificationManager.add(e, "", 6000);
					}
				}
			}
			if (PitConfig.hud().countdownEnabled && timeDiff <= 10000 && timeDiff > 0) {
				int secondsLeft = (int) Math.ceil(timeDiff / 1000.0);
				String tickKey = eventKey + "_sec_" + secondsLeft;
				if (!countdownTicks.contains(tickKey)) {
					countdownTicks.add(tickKey);
					mc.player.playSound(SoundEvents.NOTE_BLOCK_PLING.value(), 1.0F, secondsLeft <= 3 ? 1.5F : 1.0F);
					String colorCode = secondsLeft <= 3 ? "§c§l" : "§e§l";
					mc.gui.setTimes(2, 16, 2);
					mc.gui.setTitle(Component.literal(colorCode + secondsLeft));
					mc.gui.setSubtitle(Component.literal("§7" + e.event + " is starting!"));
				}
			}
		}
	}
}
