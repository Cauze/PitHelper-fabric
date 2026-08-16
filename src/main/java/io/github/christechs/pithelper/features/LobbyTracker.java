package io.github.christechs.pithelper.features;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.github.christechs.pithelper.compat.McCompat;
import io.github.christechs.pithelper.config.PitConfig;
import io.github.christechs.pithelper.data.HypixelGameMode;
import io.github.christechs.pithelper.utils.ChatUtil;
import io.github.christechs.pithelper.utils.ServerState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.TeamColor;

public class LobbyTracker {
	private static final Pattern STRIP_COLOR = Pattern.compile("(?i)§[0-9A-FK-OR]");
	private static final Pattern CHAT_PRESTIGE_PATTERN = Pattern.compile("\\[(?:([IVXLCDM]+)-)?\\d+\\]\\s*(?:[^a-zA-Z0-9\\s\\[\\]]+\\s*)?(?:\\[[A-Za-z+]+\\]\\s*)?([a-zA-Z0-9_]{2,16})");
	private static final String[] BRACKET_NAMES = {
		"§7[0]", "§9[1-4]", "§e[5-9]", "§6[10-14]", "§c[15-19]", "§5[20-24]",
		"§d[25-29]", "§f[30-34]", "§b[35-39]", "§1[40-44]", "§0[45-47]", "§4[48-49]", "§8[50]"
	};
	public static LobbyTracker INSTANCE;
	private final Set<String> knownLobbyUuids = new HashSet<>();
	private final Map<String, Integer> exactPrestiges = new HashMap<>();
	private long lastScanTime = 0;
	private double lastAveragePrestige = 0.0;
	private boolean firstScanDone = false;

	public LobbyTracker() {
		INSTANCE = this;
	}

	public void printManualStats() {
		if (!ServerState.onHypixel || ServerState.currentGameMode != HypixelGameMode.PIT) {
			return;
		}
		scanLobby(Minecraft.getInstance(), true);
	}

	public void onTick(Minecraft mc) {
		if (mc.player == null || mc.getConnection() == null || mc.level == null) {
			return;
		}
		if (!ServerState.onHypixel || ServerState.currentGameMode != HypixelGameMode.PIT) {
			knownLobbyUuids.clear();
			exactPrestiges.clear();
			lastAveragePrestige = 0.0;
			firstScanDone = false;
			return;
		}
		long now = System.currentTimeMillis();
		if (now - lastScanTime > 3000) {
			scanLobby(mc, false);
			lastScanTime = now;
		}
	}

	private void scanLobby(Minecraft mc, boolean forcePrint) {
		if (mc.getConnection() == null) {
			return;
		}

		Set<String> currentUuids = new HashSet<>();
		int totalPrestige = 0;
		int validPlayers = 0;
		int[] brackets = new int[13];
		List<String> friendsInLobby = new ArrayList<>();
		List<String> enemiesInLobby = new ArrayList<>();

		for (PlayerInfo info : mc.getConnection().getListedOnlinePlayers()) {
			String uuid = info.getProfile().id().toString().replace("-", "");
			String rawName = info.getProfile().name();
			currentUuids.add(uuid);

			if (!firstScanDone) {
				if (PitConfig.social().friends.containsKey(uuid)) {
					friendsInLobby.add(rawName);
				}
				if (PitConfig.social().enemies.containsKey(uuid)) {
					enemiesInLobby.add(rawName);
				}
			} else if (!knownLobbyUuids.contains(uuid)) {
				if (PitConfig.social().notifyFriends && PitConfig.social().friends.containsKey(uuid)) {
					McCompat.chat("§a§l[Friend] §e" + rawName + " §ajoined the lobby!");
				} else if (PitConfig.social().notifyEnemies && PitConfig.social().enemies.containsKey(uuid)) {
					McCompat.chat("§c§l[Enemy] §e" + rawName + " §cjoined the lobby!");
				}
			}

			int prestige = exactPrestiges.getOrDefault(rawName, getApproxPrestigeFromTeam(info.getTeam()));
			totalPrestige += prestige;
			validPlayers++;
			brackets[getBracketIndex(prestige)]++;
		}

		knownLobbyUuids.clear();
		knownLobbyUuids.addAll(currentUuids);

		if (!firstScanDone) {
			if (PitConfig.social().notifyFriends && !friendsInLobby.isEmpty()) {
				McCompat.chat("§a§l[Friends in Lobby] §7" + String.join(", ", friendsInLobby));
			}
			if (PitConfig.social().notifyEnemies && !enemiesInLobby.isEmpty()) {
				McCompat.chat("§c§l[Enemies in Lobby] §7" + String.join(", ", enemiesInLobby));
			}
		}

		if (validPlayers > 0 && PitConfig.social().lobbyScanner) {
			double currentAverage = (double) totalPrestige / validPlayers;
			if (forcePrint) {
				printLobbyStats(currentAverage, validPlayers, brackets, false, true);
			} else if (!firstScanDone) {
				printLobbyStats(currentAverage, validPlayers, brackets, false, false);
				lastAveragePrestige = currentAverage;
				firstScanDone = true;
			} else if (Math.abs(currentAverage - lastAveragePrestige) >= 2.0) {
				printLobbyStats(currentAverage, validPlayers, brackets, true, false);
				lastAveragePrestige = currentAverage;
			}
		} else if (!firstScanDone) {
			firstScanDone = true;
		}
	}

	private void printLobbyStats(double avg, int total, int[] brackets, boolean isUpdate, boolean manual) {
		String prefix = manual ? "§6§lSTATS" : "§b§lLOBBY";
		String type = isUpdate ? "§fShifted" : "§fPrestige";
		McCompat.chat("§8§m----------------------------------------");
		McCompat.chat(prefix + " §8» " + type + ": §e" + String.format("%.2f", avg) + " §7(" + total + " players)");
		for (int i = 0; i < brackets.length; i++) {
			if (brackets[i] > 0) {
				int pct = (int) Math.round((brackets[i] / (double) total) * 100);
				McCompat.chat(" §7• " + BRACKET_NAMES[i] + " §8» §f" + pct + "% §7(" + brackets[i] + ")");
			}
		}
		McCompat.chat("§8§m----------------------------------------");
	}

	private int getBracketIndex(int p) {
		if (p == 0) return 0;
		if (p <= 4) return 1;
		if (p <= 9) return 2;
		if (p <= 14) return 3;
		if (p <= 19) return 4;
		if (p <= 24) return 5;
		if (p <= 29) return 6;
		if (p <= 34) return 7;
		if (p <= 39) return 8;
		if (p <= 44) return 9;
		if (p <= 47) return 10;
		if (p <= 49) return 11;
		return 12;
	}

	private int getApproxPrestigeFromTeam(PlayerTeam team) {
		if (team == null) {
			return 0;
		}
		return switch (team.getColor().orElse(null)) {
			case GRAY -> 0;
			case BLUE -> 2;
			case YELLOW -> 7;
			case GOLD -> 12;
			case RED -> 17;
			case DARK_PURPLE -> 22;
			case LIGHT_PURPLE -> 27;
			case WHITE -> 32;
			case AQUA -> 37;
			case DARK_BLUE -> 42;
			case BLACK -> 46;
			case DARK_RED -> 48;
			case DARK_GRAY -> 50;
			case null, default -> 0;
		};
	}

	private int romanToInt(String s) {
		int total = 0;
		int prev = 0;
		for (int i = s.length() - 1; i >= 0; i--) {
			int value = switch (s.charAt(i)) {
				case 'I' -> 1;
				case 'V' -> 5;
				case 'X' -> 10;
				case 'L' -> 50;
				case 'C' -> 100;
				case 'D' -> 500;
				case 'M' -> 1000;
				default -> 0;
			};
			if (value < prev) {
				total -= value;
			} else {
				total += value;
			}
			prev = value;
		}
		return total;
	}

	public void onChat(String raw) {
		if (!ServerState.onHypixel || ServerState.currentGameMode != HypixelGameMode.PIT) {
			return;
		}
		String clean = STRIP_COLOR.matcher(raw).replaceAll("").trim();
		Matcher m = CHAT_PRESTIGE_PATTERN.matcher(clean);
		while (m.find()) {
			String roman = m.group(1);
			String name = m.group(2);
			int prestige = roman != null ? romanToInt(roman) : 0;
			if (prestige != 0) {
				Integer existing = exactPrestiges.get(name);
				if (existing == null || existing != prestige) {
					exactPrestiges.put(name, prestige);
					scanLobby(Minecraft.getInstance(), false);
				}
			}
		}
	}

	public static Component recolorMessage(Component message) {
		if (message == null || !ServerState.onHypixel || ServerState.currentGameMode != HypixelGameMode.PIT) {
			return message;
		}
		String formatted = ChatUtil.toLegacy(message);
		boolean modified = false;
		if (PitConfig.social().highlightFriends) {
			for (String friendName : PitConfig.social().friends.values()) {
				if (!friendName.isEmpty() && formatted.contains(friendName)) {
					formatted = formatted.replace(friendName, "§a§l" + friendName + "§r");
					modified = true;
				}
			}
		}
		if (PitConfig.social().highlightEnemies) {
			for (String enemyName : PitConfig.social().enemies.values()) {
				if (!enemyName.isEmpty() && formatted.contains(enemyName)) {
					formatted = formatted.replace(enemyName, "§c§l" + enemyName + "§r");
					modified = true;
				}
			}
		}
		return modified ? ChatUtil.legacy(formatted) : message;
	}
}
