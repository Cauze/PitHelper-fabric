/*
 * PitHelper - A Hypixel The Pit Helper Mod.
 * Copyright (C) 2026 Christian Steenkamp
 */

package io.github.christechs.pithelper.ui.utils;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.resources.Identifier;

public class IconCache {
	private static Map<String, Identifier> cache;

	public static Identifier get(String eventName) {
		if (cache == null) {
			cache = new HashMap<>();
			cache.put("Quick Maths", item("paper"));
			cache.put("KOTH", item("stick"));
			cache.put("KOTL", block("ladder"));
			cache.put("Team Deathmatch", item("diamond_sword"));
			cache.put("Rage Pit", item("blaze_powder"));
			cache.put("Beast", item("diamond_chestplate"));
			cache.put("2x Rewards", item("experience_bottle"));
			cache.put("Auction", item("emerald"));
			cache.put("All bounty", block("dead_bush"));
			cache.put("Robbery", item("gold_ingot"));
			cache.put("Care Package", item("chest_minecart"));
			cache.put("Raffle", item("name_tag"));
			cache.put("Squads", item("red_bed"));
			cache.put("Spire", item("ender_pearl"));
			cache.put("Giant Cake", item("cake"));
			cache.put("Pizza", Identifier.fromNamespaceAndPath("pithelper", "textures/pizza.png"));
			cache.put("Dragon Egg", item("egg"));
			cache.put("Blockhead", block("dirt"));
		}
		return cache.getOrDefault(eventName, item("book"));
	}

	private static Identifier item(String name) {
		return Identifier.fromNamespaceAndPath("minecraft", "textures/item/" + name + ".png");
	}

	private static Identifier block(String name) {
		return Identifier.fromNamespaceAndPath("minecraft", "textures/block/" + name + ".png");
	}
}
