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

package io.github.christechs.pithelper.utils;

import java.util.HashMap;
import java.util.Map;

public class EnchantDictionary {
    private static final Map<String, String> FRIENDLY_TO_INTERNAL = new HashMap<>();
    private static final Map<String, String> INTERNAL_TO_FRIENDLY = new HashMap<>();

    static {
        add("Sharp", "plain_melee_damage");
        add("Bounty Reaper", "melee_damage_vs_bounties");
        add("Diamond Stomp", "melee_damage_vs_diamond");
        add("Beat the Spammers", "melee_damage_vs_bows");
        add("Punisher", "melee_damage_vs_low_hp");
        add("King Buster", "melee_damage_vs_high_hp");
        add("Pain Focus", "melee_damage_when_low");
        add("Gold and Boosted", "melee_damage_when_absorption");
        add("Grasshopper", "melee_damage_when_on_grass");
        add("Shark", "melee_damage_when_close_low_players");
        add("Fancy Raider", "melee_damage_vs_leather");
        add("Bruiser", "increased_blocking");
        add("Berserker", "melee_crit_midair");
        add("Speedy Kill", "etspeed_on_kill");
        add("Counter-Janitor", "resistance_on_kill");
        add("Guts", "melee_heal_on_kill");
        add("Combo: Damage", "melee_combo_damage");
        add("Combo: Heal", "melee_combo_heal");
        add("Combo: Swift", "melee_combo_speed");
        add("Crush", "melee_weakness");
        add("Lifesteal", "melee_heal_on_hit");
        add("Knockback", "melee_knockback");
        add("Bullet Time", "blocking_cancels_projectiles");
        add("Revengeance", "melee_avenge");
        add("Duelist", "melee_strike_after_block");
        add("Hemorrhage", "melee_bleed");
        add("Combo: Perun's Wrath", "melee_lightning");
        add("Executioner", "melee_execute");
        add("Speedy Hit", "melee_speed_on_hit");
        add("The Punch", "melee_launch");
        add("Gamble", "melee_gamble");
        add("Combo: Stun", "melee_stun");
        add("Billionaire", "melee_literally_p2w");
        add("Healer", "melee_healer");

        add("Bottomless Quiver", "gain_arrows_on_hit");
        add("Jumpspammer", "jump_spammer");
        add("Fletching", "bow_damage");
        add("First Shot", "first_shot");
        add("Sniper", "sniper");
        add("Spammer and Proud", "bow_spammer");
        add("Arrow Armory", "damage_per_arrow");
        add("Push comes to shove", "punch_once_in_a_while");
        add("Parasite", "parasite");
        add("Faster than their shadow", "bow_combo_speed");
        add("What doesn't kill you", "heal_on_shoot_self");
        add("Wasp", "bow_weakness_on_hit");
        add("Mixed Combat", "mixed_combat");
        add("Sprint Drain", "bow_slow");
        add("Chipping", "arrow_true_damage");
        add("Volley", "volley");
        add("Mega Longbow", "instant_shot");
        add("Pin down", "pin_down");
        add("Explosive", "explosive_bow");
        add("Pullbow", "pullbow");
        add("Robinhood", "homing");
        add("True Shot", "bow_to_true_damage");
        add("Devil Chicks!", "explosive_chickens");
        add("Telebow", "telebow");
        add("Lucky Shot", "lucky_shot");

        add("Protection", "damage_reduction");
        add("Diamond Allergy", "less_damage_vs_diamond_weapons");
        add("Ring Armor", "less_damage_from_arrows");
        add("Billy", "less_damage_when_high_bounty");
        add("Not Gladiator", "less_damage_nearby_players");
        add("Cricket", "less_damage_on_grass");
        add("McSwimmer", "less_damage_when_swimming");
        add("David and Goliath", "less_damage_vs_bounties");
        add("Eggs", "eggs");
        add("Peroxide", "regen_when_hit");
        add("Last Stand", "resistance_when_low");
        add("Revitalize", "regen_speed_when_low");
        add("Gotta go fast", "perma_speed");
        add("TNT", "tnt");
        add("Counter-Offensive", "speed_when_hit_few_times");
        add("Hearts", "higher_max_hp");
        add("Boo-boo", "passive_health_regen");
        add("Mirror", "immune_true_damage");
        add("Creative", "wood_blocks");
        add("Prick", "thorns");
        add("Critically Funky", "power_against_crits");
        add("Hunt the Hunter", "counter_bounty_hunter");
        add("Excess", "overheal_enchant");
        add("Respawn: Resistance", "respawn_with_resistance");
        add("Respawn: Absorption", "respawn_with_absorption");
        add("Electrolytes", "refresh_speed_on_kill");
        add("Danger Close", "superspeed_when_low");
        add("Golden Heart", "absorption_on_kill");
        add("Steaks", "steaks_on_kill");
        add("Snowballs", "snowballs");
        add("Martyrdom", "martyrdom");
        add("Wolf Pack", "wolf_pack");
        add("Snowmen Army", "snowmen");
        add("Pit Blob", "the_blob");
        add("Instaboom", "instaboom_tnt");
        add("Escape Pod", "escape_pod");
        add("Singularity", "singularity");
        add("Solitude", "solitude");
        add("Phoenix", "phoenix");
        add("Gomraw's Heart", "regen_when_ooc");
        add("Divine Miracle", "chance_dont_lose_life");
        add("Double-jump", "double_jump");
        add("Assassin", "sneak_teleport");

        add("Gold Bump", "gold_per_kill");
        add("Gold Boost", "gold_boost");
        add("Moctezuma", "gold_strictly_kills");
        add("XP Bump", "xp_per_kill");
        add("XP Boost", "xp_boost");
        add("Strike Gold", "gold_per_hit");
        add("Critically Rich", "gold_per_crit");
        add("Pebble", "increase_gold_pickup");
        add("Sweaty", "streak_xp");
        add("Lodbrok", "increase_armor_drops");
        add("Pitpocket", "pickpocket");
        add("Pants Radar", "pants_radar");
        add("Sierra", "gold_per_diamond_piece");
        add("Purple Gold", "gold_break_obsidian");
        add("Self-checkout", "max_bounty_self_claim");
        add("Negotiator", "contract_rewards");
        add("Club Rod", "fishing_rod_enchant");
        add("Luck of the Pond", "luck_of_the_pond");
        add("Rogue", "rogue");
        add("Rodback", "fishing_rod_kb");
        add("Grandmaster", "rod_true_damage");
        add("Portable Pond", "water_bucket");
        add("Unite", "fishers_unite");
        add("Trophy", "trophy");
        add("Tough Crew", "tough_crew");
        add("Stereo", "stereo");
        add("Fractional Reserve", "fractional_reserve");
        add("Pit MBA", "pit_mba");
        add("Paparazzi", "paparazzi");
        add("Hidden Jewel", "hidden_jewel");
        add("Worm", "worm");
        add("Aegis", "aegis");
        add("Trash Panda", "trash_panda");
        add("Hidden Jewel Sword", "melee_hidden_jewel");
        add("Royalty", "royalty");
        add("Somber", "somber");
        add("Spite", "spite");
        add("Sanguisuge", "sanguisuge");
        add("Misery", "misery");
        add("Needless Suffering", "needless_suffering");
        add("Mind Assault", "mind_assault");
        add("Grim Reaper", "grim_reaper");
        add("Hedge Fund", "hedge_fund");
        add("Golden Handcuffs", "golden_handcuffs");
        add("Heartripper", "heartripper");
        add("Combo: Venom", "venom");
        add("Nostalgia", "nostalgia");
        add("Lycanthropy", "lycanthropy");
        add("Guardian", "guardian");
        add("Evil Within", "evil_within");
    }

    private static void add(String friendly, String internal) {
        FRIENDLY_TO_INTERNAL.put(friendly.toLowerCase(), internal);
        INTERNAL_TO_FRIENDLY.put(internal, friendly);
    }

    public static String getInternalName(String input) {
        if (input == null || input.isEmpty()) return "";
        String normalized = input.toLowerCase().trim().replace("\"", "");

        if (FRIENDLY_TO_INTERNAL.containsKey(normalized)) {
            return FRIENDLY_TO_INTERNAL.get(normalized);
        }
        if (INTERNAL_TO_FRIENDLY.containsKey(normalized)) {
            return normalized;
        }

        for (String friendly : FRIENDLY_TO_INTERNAL.keySet()) {
            if (friendly.contains(normalized)) {
                return FRIENDLY_TO_INTERNAL.get(friendly);
            }
        }
        for (String internal : INTERNAL_TO_FRIENDLY.keySet()) {
            if (internal.contains(normalized)) {
                return internal;
            }
        }

        return normalized.replace(" ", "_").replace("'", "");
    }

    public static String getFriendlyName(String internal) {
        if (internal == null || internal.isEmpty()) return "";
        if (INTERNAL_TO_FRIENDLY.containsKey(internal)) return INTERNAL_TO_FRIENDLY.get(internal);
        return internal;
    }
}
