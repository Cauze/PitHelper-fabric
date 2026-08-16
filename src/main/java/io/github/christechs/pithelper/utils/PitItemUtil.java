/*
 * PitHelper - A Hypixel The Pit Helper Mod.
 * Copyright (C) 2026 Christian Steenkamp
 */

package io.github.christechs.pithelper.utils;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.component.ItemLore;

public class PitItemUtil {
	private static final Pattern STRIP_COLOR = Pattern.compile("(?i)§[0-9A-FK-OR]");

	public static int parseSafeInt(String str, int fallback) {
		try {
			return Integer.parseInt(str);
		} catch (Exception e) {
			return fallback;
		}
	}

	public static Map<String, ItemStack[]> parseBase64Inventories(JsonObject raw) {
		Map<String, ItemStack[]> map = new HashMap<>();
		if (raw == null) {
			return map;
		}
		for (Map.Entry<String, JsonElement> entry : raw.entrySet()) {
			try {
				byte[] bytes = Base64.getDecoder().decode(entry.getValue().getAsString());
				CompoundTag root = NbtIo.readCompressed(new ByteArrayInputStream(bytes), NbtAccounter.unlimitedHeap());
				ListTag list = root.getListOrEmpty("i");
				ItemStack[] items = new ItemStack[list.size()];
				for (int i = 0; i < list.size(); i++) {
					CompoundTag tag = list.getCompoundOrEmpty(i);
					if (!tag.isEmpty()) {
						items[i] = fromLegacyNbt(tag);
					}
				}
				map.put(entry.getKey(), items);
			} catch (Exception ignored) {
			}
		}
		return map;
	}

	public static ItemStack synthesizeItem(JsonObject json) {
		int itemId = json.has("item_id") && !json.get("item_id").isJsonNull() ? json.get("item_id").getAsInt() : 300;
		Item mcItem = LegacyItems.fromNumericId(itemId);
		if (mcItem == Items.AIR || mcItem == Items.PAPER && itemId == 300) {
			mcItem = Items.LEATHER_LEGGINGS;
		}
		if (itemId == 283) {
			mcItem = Items.GOLDEN_SWORD;
		} else if (itemId == 261) {
			mcItem = Items.BOW;
		} else if (itemId == 300) {
			mcItem = Items.LEATHER_LEGGINGS;
		}

		ItemStack stack = new ItemStack(mcItem, 1);
		if (json.has("custom_name") && !json.get("custom_name").isJsonNull()) {
			stack.set(DataComponents.CUSTOM_NAME, ChatUtil.legacy(json.get("custom_name").getAsString()));
		}

		List<Component> lore = new ArrayList<>();
		if (json.has("owner_uuid") && !json.get("owner_uuid").isJsonNull()) {
			String uuid = json.get("owner_uuid").getAsString();
			lore.add(ChatUtil.legacy("§7Owner: " + (uuid.length() > 8 ? uuid.substring(0, 8) + "..." : uuid)));
			lore.add(Component.empty());
		}
		if (json.has("enchants") && json.get("enchants").isJsonObject()) {
			JsonObject enchants = json.getAsJsonObject("enchants");
			for (Map.Entry<String, JsonElement> e : enchants.entrySet()) {
				String friendly = EnchantDictionary.getFriendlyName(e.getKey());
				lore.add(ChatUtil.legacy("§9" + friendly + " " + e.getValue().getAsInt()));
			}
		}
		if (json.has("lives") && !json.get("lives").isJsonNull() && json.has("max_lives") && !json.get("max_lives").isJsonNull()) {
			lore.add(Component.empty());
			lore.add(ChatUtil.legacy("§cLives: " + json.get("lives").getAsInt() + "/" + json.get("max_lives").getAsInt()));
		}
		lore.add(Component.empty());
		lore.add(ChatUtil.legacy("§eClick to view owner profile"));
		stack.set(DataComponents.LORE, new ItemLore(lore));
		return stack;
	}

	public static ItemStack withOwnerLore(ItemStack stack, String ownerName) {
		if (stack == null || stack.isEmpty()) {
			return stack;
		}
		ItemStack copy = stack.copy();
		List<Component> lines = new ArrayList<>();
		ItemLore existing = copy.get(DataComponents.LORE);
		if (existing != null) {
			lines.addAll(existing.lines());
		}
		lines.add(Component.empty());
		lines.add(ChatUtil.legacy("§7Owner: §e" + ownerName));
		lines.add(ChatUtil.legacy("§eClick to view profile"));
		copy.set(DataComponents.LORE, new ItemLore(lines));
		return copy;
	}

	public static boolean itemMatchesFilters(ItemStack stack, String query, String enchant, String type, int minLives, int maxLives) {
		if (stack == null || stack.isEmpty()) {
			return false;
		}

		String qGeneral = query == null ? "" : query.toLowerCase().trim();
		String qEnchant = enchant == null ? "" : enchant.toLowerCase().trim();
		String qType = type == null ? "" : type.toLowerCase().trim();

		if (qGeneral.isEmpty() && qEnchant.isEmpty() && qType.isEmpty() && minLives == -1 && maxLives == -1) {
			return false;
		}

		if (!qGeneral.isEmpty() || !qEnchant.isEmpty() || minLives != -1 || maxLives != -1) {
			boolean generalFound = qGeneral.isEmpty() || stack.getHoverName().getString().toLowerCase().contains(qGeneral);
			boolean enchantFound = qEnchant.isEmpty() || stack.getHoverName().getString().toLowerCase().contains(qEnchant);
			int sLives = -1;

			try {
				Minecraft mc = Minecraft.getInstance();
				List<Component> tooltip = stack.getTooltipLines(
					Item.TooltipContext.of(mc.level),
					mc.player,
					TooltipFlag.Default.NORMAL
				);
				for (Component line : tooltip) {
					String clean = STRIP_COLOR.matcher(line.getString()).replaceAll("");
					String lowerClean = clean.toLowerCase();
					if (!qGeneral.isEmpty() && lowerClean.contains(qGeneral)) {
						generalFound = true;
					}
					if (!qEnchant.isEmpty() && lowerClean.contains(qEnchant)) {
						enchantFound = true;
					}
					if (clean.startsWith("Lives: ")) {
						String[] parts = clean.replace("Lives: ", "").split("/");
						if (parts.length == 2) {
							sLives = Integer.parseInt(parts[0].trim());
						}
					}
				}
			} catch (Exception ignored) {
			}

			if (!generalFound) {
				return false;
			}
			if (!enchantFound) {
				return false;
			}
			if (minLives != -1 && sLives < minLives) {
				return false;
			}
			if (maxLives != -1 && sLives > maxLives) {
				return false;
			}
		}

		if (!qType.isEmpty()) {
			Item item = stack.getItem();
			if (qType.contains("sword") && item != Items.GOLDEN_SWORD) {
				return false;
			}
			if (qType.contains("bow") && item != Items.BOW) {
				return false;
			}
			return !qType.contains("pant") || item == Items.LEATHER_LEGGINGS;
		}

		return true;
	}

	private static ItemStack fromLegacyNbt(CompoundTag tag) {
		Item item = Items.PAPER;
		int count = 1;
		Tag idTag = tag.get("id");
		if (idTag instanceof NumericTag numeric) {
			item = LegacyItems.fromNumericId(numeric.intValue());
		} else if (idTag instanceof StringTag) {
			item = LegacyItems.fromName(tag.getStringOr("id", "minecraft:paper"));
		}
		count = Math.max(1, tag.getByteOr("Count", (byte) 1));
		ItemStack stack = new ItemStack(item, count);

		CompoundTag extra = tag.getCompoundOrEmpty("tag");
		if (extra.isEmpty()) {
			return stack;
		}
		CompoundTag display = extra.getCompoundOrEmpty("display");
		if (!display.isEmpty()) {
			String name = display.getStringOr("Name", "");
			if (!name.isEmpty()) {
				stack.set(DataComponents.CUSTOM_NAME, ChatUtil.legacy(name));
			}
			ListTag loreTag = display.getListOrEmpty("Lore");
			if (!loreTag.isEmpty()) {
				List<Component> lore = new ArrayList<>();
				for (int i = 0; i < loreTag.size(); i++) {
					lore.add(ChatUtil.legacy(loreTag.getString(i).orElse("")));
				}
				stack.set(DataComponents.LORE, new ItemLore(lore));
			}
			if (display.contains("color")) {
				stack.set(DataComponents.DYED_COLOR, new DyedItemColor(display.getIntOr("color", 0)));
			}
		}
		return stack;
	}
}
