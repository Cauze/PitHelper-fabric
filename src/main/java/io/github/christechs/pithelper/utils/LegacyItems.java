/*
 * PitHelper - A Hypixel The Pit Helper Mod.
 * Copyright (C) 2026 Christian Steenkamp
 */

package io.github.christechs.pithelper.utils;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

final class LegacyItems {
	private static final Map<Integer, String> BY_ID = new HashMap<>();

	static {
		put(1, "stone");
		put(3, "dirt");
		put(4, "cobblestone");
		put(5, "oak_planks");
		put(17, "oak_log");
		put(20, "glass");
		put(24, "sandstone");
		put(35, "white_wool");
		put(41, "gold_block");
		put(42, "iron_block");
		put(45, "bricks");
		put(46, "tnt");
		put(47, "bookshelf");
		put(49, "obsidian");
		put(54, "chest");
		put(57, "diamond_block");
		put(58, "crafting_table");
		put(65, "ladder");
		put(80, "snow_block");
		put(81, "cactus");
		put(88, "soul_sand");
		put(89, "glowstone");
		put(103, "melon");
		put(116, "enchanting_table");
		put(130, "ender_chest");
		put(133, "emerald_block");
		put(138, "beacon");
		put(145, "anvil");
		put(152, "redstone_block");
		put(154, "hopper");
		put(155, "quartz_block");
		put(165, "slime_block");
		put(168, "prismarine");
		put(169, "sea_lantern");
		put(173, "coal_block");
		put(256, "iron_shovel");
		put(257, "iron_pickaxe");
		put(258, "iron_axe");
		put(259, "flint_and_steel");
		put(260, "apple");
		put(261, "bow");
		put(262, "arrow");
		put(263, "coal");
		put(264, "diamond");
		put(265, "iron_ingot");
		put(266, "gold_ingot");
		put(267, "iron_sword");
		put(268, "wooden_sword");
		put(269, "wooden_shovel");
		put(270, "wooden_pickaxe");
		put(271, "wooden_axe");
		put(272, "stone_sword");
		put(273, "stone_shovel");
		put(274, "stone_pickaxe");
		put(275, "stone_axe");
		put(276, "diamond_sword");
		put(277, "diamond_shovel");
		put(278, "diamond_pickaxe");
		put(279, "diamond_axe");
		put(280, "stick");
		put(281, "bowl");
		put(282, "mushroom_stew");
		put(283, "golden_sword");
		put(284, "golden_shovel");
		put(285, "golden_pickaxe");
		put(286, "golden_axe");
		put(287, "string");
		put(288, "feather");
		put(289, "gunpowder");
		put(290, "wooden_hoe");
		put(291, "stone_hoe");
		put(292, "iron_hoe");
		put(293, "diamond_hoe");
		put(294, "golden_hoe");
		put(295, "wheat_seeds");
		put(296, "wheat");
		put(297, "bread");
		put(298, "leather_helmet");
		put(299, "leather_chestplate");
		put(300, "leather_leggings");
		put(301, "leather_boots");
		put(302, "chainmail_helmet");
		put(303, "chainmail_chestplate");
		put(304, "chainmail_leggings");
		put(305, "chainmail_boots");
		put(306, "iron_helmet");
		put(307, "iron_chestplate");
		put(308, "iron_leggings");
		put(309, "iron_boots");
		put(310, "diamond_helmet");
		put(311, "diamond_chestplate");
		put(312, "diamond_leggings");
		put(313, "diamond_boots");
		put(314, "golden_helmet");
		put(315, "golden_chestplate");
		put(316, "golden_leggings");
		put(317, "golden_boots");
		put(318, "flint");
		put(319, "porkchop");
		put(320, "cooked_porkchop");
		put(321, "painting");
		put(322, "golden_apple");
		put(323, "oak_sign");
		put(324, "oak_door");
		put(325, "bucket");
		put(326, "water_bucket");
		put(327, "lava_bucket");
		put(328, "minecart");
		put(329, "saddle");
		put(330, "iron_door");
		put(331, "redstone");
		put(332, "snowball");
		put(333, "oak_boat");
		put(334, "leather");
		put(335, "milk_bucket");
		put(336, "brick");
		put(337, "clay_ball");
		put(338, "sugar_cane");
		put(339, "paper");
		put(340, "book");
		put(341, "slime_ball");
		put(342, "chest_minecart");
		put(343, "furnace_minecart");
		put(344, "egg");
		put(345, "compass");
		put(346, "fishing_rod");
		put(347, "clock");
		put(348, "glowstone_dust");
		put(349, "cod");
		put(350, "cooked_cod");
		put(351, "ink_sac");
		put(352, "bone");
		put(353, "sugar");
		put(354, "cake");
		put(355, "red_bed");
		put(356, "repeater");
		put(357, "cookie");
		put(358, "filled_map");
		put(359, "shears");
		put(360, "melon_slice");
		put(361, "pumpkin_seeds");
		put(362, "melon_seeds");
		put(363, "beef");
		put(364, "cooked_beef");
		put(365, "chicken");
		put(366, "cooked_chicken");
		put(367, "rotten_flesh");
		put(368, "ender_pearl");
		put(369, "blaze_rod");
		put(370, "ghast_tear");
		put(371, "gold_nugget");
		put(372, "nether_wart");
		put(373, "potion");
		put(374, "glass_bottle");
		put(375, "spider_eye");
		put(376, "fermented_spider_eye");
		put(377, "blaze_powder");
		put(378, "magma_cream");
		put(379, "brewing_stand");
		put(380, "cauldron");
		put(381, "ender_eye");
		put(382, "glistering_melon_slice");
		put(384, "experience_bottle");
		put(385, "fire_charge");
		put(386, "writable_book");
		put(387, "written_book");
		put(388, "emerald");
		put(389, "item_frame");
		put(390, "flower_pot");
		put(391, "carrot");
		put(392, "potato");
		put(393, "baked_potato");
		put(394, "poisonous_potato");
		put(395, "map");
		put(396, "golden_carrot");
		put(397, "player_head");
		put(398, "carrot_on_a_stick");
		put(399, "nether_star");
		put(400, "pumpkin_pie");
		put(401, "firework_rocket");
		put(402, "firework_star");
		put(403, "enchanted_book");
		put(404, "comparator");
		put(405, "nether_brick");
		put(406, "quartz");
		put(407, "tnt_minecart");
		put(408, "hopper_minecart");
		put(409, "prismarine_shard");
		put(410, "prismarine_crystals");
		put(411, "rabbit");
		put(412, "cooked_rabbit");
		put(413, "rabbit_stew");
		put(414, "rabbit_foot");
		put(415, "rabbit_hide");
		put(416, "armor_stand");
		put(417, "iron_horse_armor");
		put(418, "golden_horse_armor");
		put(419, "diamond_horse_armor");
		put(420, "lead");
		put(421, "name_tag");
		put(423, "mutton");
		put(424, "cooked_mutton");
		put(425, "white_banner");
	}

	private LegacyItems() {}

	private static void put(int id, String path) {
		BY_ID.put(id, path);
	}

	static Item fromNumericId(int id) {
		String path = BY_ID.get(id);
		if (path == null) {
			return Items.PAPER;
		}
		return BuiltInRegistries.ITEM.getValue(Identifier.withDefaultNamespace(path));
	}

	static Item fromName(String id) {
		String path = id;
		if (path.startsWith("minecraft:")) {
			path = path.substring("minecraft:".length());
		}
		Item item = BuiltInRegistries.ITEM.getValue(Identifier.withDefaultNamespace(path));
		return item == Items.AIR ? Items.PAPER : item;
	}
}
