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

public enum HypixelGameMode {
    PIT("The Hypixel Pit", "PIT", "pit"),
    MAIN("Main Lobby", "MAIN", "lobby"),
    PROTOTYPE("Prototype", "PROTOTYPE", "prototype"),
    HOUSING("Housing", "HOUSING", "housing"),
    SMP("SMP", "SMP", "smp"),
    REPLAY("Replay", "REPLAY", "replay"),

    SKYBLOCK("SkyBlock", "SKYBLOCK", "skyblock"),
    BEDWARS("Bed Wars", "BEDWARS", "bedwars"),
    SKYWARS("SkyWars", "SKYWARS", "skywars"),
    MURDER_MYSTERY("Murder Mystery", "MURDER_MYSTERY", "murder"),
    DUELS("Duels", "DUELS", "duels"),

    MCGO("Cops and Crims", "MCGO", "mcgo"),
    QUAKECRAFT("Quakecraft", "QUAKECRAFT", "quake"),
    PAINTBALL("Paintball", "PAINTBALL", "paintball"),
    VAMPIREZ("VampireZ", "VAMPIREZ", "vampirez"),
    ARENA("Arena Brawl", "ARENA", "arena"),
    WALLS("The Walls", "WALLS", "walls"),
    WALLS3("Mega Walls", "WALLS3", "megawalls"),
    LEGACY("Classic Games", "LEGACY", "legacy"),

    ARCADE("Arcade", "ARCADE", "arcade"),
    TNTGAMES("The TNT Games", "TNTGAMES", "tnt"),
    BUILD_BATTLE("Build Battle", "BUILD_BATTLE", "build"),
    WOOL_GAMES("Wool Games", "WOOL_GAMES", "wool"),

    UHC("UHC Champions", "UHC", "uhc"),
    SPEED_UHC("Speed UHC", "SPEED_UHC", "speeduhc"),
    SURVIVAL_GAMES("Blitz SG", "SURVIVAL_GAMES", "blitz"),
    BATTLEGROUND("Warlords", "BATTLEGROUND", "warlords"),
    SUPER_SMASH("Smash Heroes", "SUPER_SMASH", "supersmash"),
    GINGERBREAD("Turbo Kart Racers", "GINGERBREAD", "gingerbread"),
    SKYCLASH("SkyClash", "SKYCLASH", "skyclash"),
    TRUE_COMBAT("Crazy Walls", "TRUE_COMBAT", "crazywalls"),

    UNKNOWN("Unknown", "UNKNOWN", "");

    private static final HypixelGameMode[] VALUES = values();
    public final String niceName;
    public final String serverType;
    public final String fallbackLobbyPrefix;

    HypixelGameMode(String niceName, String serverType) {
        this(niceName, serverType, "");
    }

    HypixelGameMode(String niceName, String serverType, String fallbackLobbyPrefix) {
        this.niceName = niceName;
        this.serverType = serverType;
        this.fallbackLobbyPrefix = fallbackLobbyPrefix;
    }

    public static HypixelGameMode fromPacket(String serverType, String lobbyName) {
        if (serverType != null) {
            for (HypixelGameMode mode : VALUES) {
                if (mode.serverType.equalsIgnoreCase(serverType)) {
                    return mode;
                }
            }
        }

        // fallback
        if (lobbyName != null) {
            String lowerLobby = lobbyName.toLowerCase();
            for (HypixelGameMode mode : VALUES) {
                if (!mode.fallbackLobbyPrefix.isEmpty()
                        && lowerLobby.startsWith(mode.fallbackLobbyPrefix)) {
                    return mode;
                }
            }
        }

        return UNKNOWN;
    }
}
