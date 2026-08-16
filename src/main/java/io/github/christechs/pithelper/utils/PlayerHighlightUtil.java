/*
 * PitHelper - A Hypixel The Pit Helper Mod.
 * Copyright (C) 2026 Christian Steenkamp
 */

package io.github.christechs.pithelper.utils;

import io.github.christechs.pithelper.config.PitConfig;
import io.github.christechs.pithelper.data.HypixelGameMode;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class PlayerHighlightUtil {
	private static final ThreadLocal<Integer> CURRENT = ThreadLocal.withInitial(() -> -1);

	public static void push(int color) {
		CURRENT.set(color);
	}

	public static void pop() {
		CURRENT.set(-1);
	}

	public static int current() {
		Integer color = CURRENT.get();
		return color == null ? -1 : color;
	}

	public static int getHighlightColor(Player player) {
		String uuid = player.getUUID().toString().replace("-", "");

		if (PitConfig.social().highlightFriends && PitConfig.social().friends.containsKey(uuid)) {
			return 0x55FF55;
		}
		if (PitConfig.social().highlightEnemies && PitConfig.social().enemies.containsKey(uuid)) {
			return 0xFF5555;
		}

		if (ServerState.onHypixel && ServerState.currentGameMode == HypixelGameMode.PIT) {
			int ironCount = 0;
			int chainCount = 0;
			int diamondCount = 0;
			int airCount = 0;

			for (EquipmentSlot slot : new EquipmentSlot[]{
				EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
			}) {
				ItemStack stack = player.getItemBySlot(slot);
				if (stack.isEmpty()) {
					airCount++;
					continue;
				}
				String path = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
				if (path.startsWith("diamond_")) {
					diamondCount++;
				} else if (path.startsWith("iron_")) {
					ironCount++;
				} else if (path.startsWith("chainmail_")) {
					chainCount++;
				}
			}

			if (diamondCount == 0
				&& (ironCount > 0 || chainCount > 0)
				&& (ironCount + airCount + chainCount) == 4) {
				if (PitConfig.social().highlightIron && ironCount >= 2) {
					return 0xFF55FF;
				}
				if (PitConfig.social().highlightChain && ironCount <= 1) {
					return 0xFF55FF;
				}
			}
		}

		return -1;
	}
}
