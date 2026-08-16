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

package io.github.christechs.pithelper.data;

public enum PitEventType {
    // Major Events
    PIZZA(
            "Pizza",
            3 * 60 * 1000L,
            5 * 60 * 1000L,
            "Deliver pizzas to villagers to earn cash. Most cash deposited wins."),
    RAFFLE(
            "Raffle",
            3 * 60 * 1000L,
            5 * 60 * 1000L,
            "Collect tickets around the map and deposit them in the central box."),
    SQUADS(
            "Squads",
            3 * 60 * 1000L,
            5 * 60 * 1000L,
            "Team up in squads of 3. Friendly fire is disabled for teammates."),
    BLOCKHEAD(
            "Blockhead",
            3 * 60 * 1000L,
            5 * 60 * 1000L,
            "Kill players to splatter your block. Most blocks on the map wins."),
    SPIRE(
            "Spire",
            3 * 60 * 1000L,
            5 * 60 * 1000L,
            "Scale 9 floors by getting kills to claim the ultimate chest."),
    ROBBERY(
            "Robbery",
            3 * 60 * 1000L,
            4 * 60 * 1000L,
            "Steal gold from others by hitting them. Largest stash at the end wins."),
    TEAM_DEATHMATCH(
            "Team Deathmatch",
            3 * 60 * 1000L,
            5 * 60 * 1000L,
            "Red vs Blue. Score points via kills and assists for your team."),
    RAGE_PIT(
            "Rage Pit",
            3 * 60 * 1000L,
            4 * 60 * 1000L,
            "The map is restricted to the middle and damage is massively increased."),
    BEAST(
            "Beast",
            3 * 60 * 1000L,
            5 * 60 * 1000L,
            "Kill a Beast to become one. Gain powerful diamond gear while a Beast."),

    // Minor Events
    QUICK_MATHS(
            "Quick Maths",
            0L,
            10 * 1000L,
            "First 5 players to solve the chat equation gain XP and Gold."),
    DRAGON_EGG(
            "Dragon Egg",
            0L,
            3 * 60 * 1000L,
            "Click the egg in the center for rewards. XP increases per click."),
    TWO_X_REWARDS(
            "2x Rewards",
            0L,
            4 * 60 * 1000L,
            "Earn 2x XP and Gold in a specific quadrant or the center."),
    CARE_PACKAGE(
            "Care Package",
            0L,
            3 * 60 * 1000L,
            "Click the dropped chest 200 times to unlock loot for everyone."),
    KOTL(
            "KOTL",
            0L,
            3 * 60 * 1000L,
            "Stand on the clay structure to earn Gold. Higher platforms pay more."),
    KOTH(
            "KOTH", 0L, 4 * 60 * 1000L, "Stand on the diamond circle to earn 4x XP and Gold from kills."),
    AUCTION(
            "Auction",
            0L,
            1 * 60 * 1000L,
            "Bid Gold on a special item. Bids in the last 20s extend the timer."),
    GIANT_CAKE(
            "Giant Cake",
            0L,
            2 * 60 * 1000L,
            "Eat slices of the giant cake to earn increasing Gold rewards."),
    ALL_BOUNTY(
            "All Bounty",
            0L,
            0L,
            "Instant: Every player receives a 100g bounty (or increases existing)."),

    NONE("None", 0L, 0L, "");

    public final String eventName;
    public final long startOffset;
    public final long duration;
    public final String description;

    PitEventType(String name, long startOffset, long duration, String description) {
        this.eventName = name;
        this.startOffset = startOffset;
        this.duration = duration;
        this.description = description;
    }

    public static PitEventType fromName(String name) {
        for (PitEventType type : values()) {
            if (type.eventName.equalsIgnoreCase(name)) return type;
        }
        if (name.toLowerCase().contains("bounty")) return ALL_BOUNTY;
        if (name.toLowerCase().contains("2x")) return TWO_X_REWARDS;

        return NONE;
    }
}
