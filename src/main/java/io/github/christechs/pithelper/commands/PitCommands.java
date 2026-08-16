package io.github.christechs.pithelper.commands;

import io.github.christechs.config.ConfigManager;
import io.github.christechs.pithelper.api.PitHelperAPI;
import io.github.christechs.pithelper.compat.McCompat;
import io.github.christechs.pithelper.config.PitConfig;
import io.github.christechs.pithelper.features.LobbyTracker;
import io.github.christechs.pithelper.features.NotificationHandler;
import io.github.christechs.pithelper.features.NotificationManager;
import io.github.christechs.pithelper.ui.screen.PitHelperMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;

import static io.github.christechs.pithelper.utils.ChatUtil.simulateChat;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

import com.mojang.brigadier.arguments.StringArgumentType;

public final class PitCommands {
	private PitCommands() {}

	public static void register() {
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
			dispatcher.register(literal("pithelper")
				.executes(ctx -> {
					NotificationHandler.pendingScreen = new PitHelperMenu();
					return 1;
				})
				.then(literal("help").executes(ctx -> {
					McCompat.chat("§6§l=== PitHelper Commands ===");
					McCompat.chat("§e/pithelper §7- Opens the main config menu.");
					McCompat.chat("§e/pitfriend <player> §7- Adds/removes a player as a friend.");
					McCompat.chat("§e/pitenemy <player> §7- Adds/removes a player as an enemy.");
					McCompat.chat("§e/pithelper lobbystats §7- Prints current lobby prestige stats.");
					McCompat.chat("§e/apiexplorer §7- Opens the PitHelper API explorer.");
					McCompat.chat("§e/viewinv <player> §7- Opens a player's Pit profile/inventory.");
					return 1;
				}))
				.then(literal("lobbystats").executes(ctx -> {
					if (LobbyTracker.INSTANCE != null) {
						LobbyTracker.INSTANCE.printManualStats();
					} else {
						McCompat.chat("§cLobby Tracker is not currently active.");
					}
					return 1;
				}))
				.then(literal("test")
					.then(literal("quickmaths").executes(ctx -> {
						simulateChat("§d§lQUICK MATHS! §eSolve: (2+8)x5");
						return 1;
					}))
					.then(literal("chat").executes(ctx -> {
						simulateChat("§6§l[PitHelper] §eTEST EVENT §7starts in §a0m 0s§7!");
						return 1;
					}))
					.then(literal("visual").executes(ctx -> {
						NotificationManager.add(null, "Mock notification test.", 5000);
						return 1;
					}))
				)
			);

			dispatcher.register(literal("pitfriend")
				.then(argument("player", StringArgumentType.word()).executes(ctx -> {
					toggleList(StringArgumentType.getString(ctx, "player"), true);
					return 1;
				})));
			dispatcher.register(literal("pitenemy")
				.then(argument("player", StringArgumentType.word()).executes(ctx -> {
					toggleList(StringArgumentType.getString(ctx, "player"), false);
					return 1;
				})));
			dispatcher.register(literal("viewinv")
				.executes(ctx -> {
					McCompat.chat("§cUsage: /viewinv <player>");
					return 0;
				})
				.then(argument("player", StringArgumentType.word()).executes(ctx -> {
					NotificationHandler.pendingScreen = new io.github.christechs.pithelper.ui.screen.CustomProfileViewerScreen(
						null, StringArgumentType.getString(ctx, "player"));
					return 1;
				})));
			dispatcher.register(literal("apiexplorer").executes(ctx -> {
				NotificationHandler.pendingScreen = new io.github.christechs.pithelper.ui.screen.ApiExplorerScreen(null);
				return 1;
			}));
		});
	}

	private static void toggleList(String name, boolean friend) {
		java.util.Map<String, String> map = friend ? PitConfig.social().friends : PitConfig.social().enemies;
		java.util.Map<String, String> other = friend ? PitConfig.social().enemies : PitConfig.social().friends;
		String existingUuid = null;
		for (String uuid : map.keySet()) {
			if (map.get(uuid).equalsIgnoreCase(name)) {
				existingUuid = uuid;
				break;
			}
		}
		if (existingUuid != null) {
			map.remove(existingUuid);
			ConfigManager.save();
			McCompat.chat("§cRemoved §e" + name + (friend ? " §cfrom friends." : " §cfrom enemies."));
			return;
		}
		Minecraft mc = Minecraft.getInstance();
		if (mc.getConnection() != null) {
			PlayerInfo info = mc.getConnection().getPlayerInfo(name);
			if (info != null) {
				String uuid = info.getProfile().id().toString().replace("-", "");
				map.put(uuid, info.getProfile().name());
				other.remove(uuid);
				ConfigManager.save();
				McCompat.chat("§aAdded §e" + info.getProfile().name() + (friend ? " §ato friends!" : " §ato enemies!"));
				return;
			}
		}
		McCompat.chat("§eResolving " + name + "...");
		java.util.concurrent.CompletableFuture.runAsync(() -> {
			try {
				String uuid = PitHelperAPI.resolveUuid(name);
				Minecraft.getInstance().execute(() -> {
					map.put(uuid, name);
					other.remove(uuid);
					ConfigManager.save();
					McCompat.chat("§aAdded §e" + name + (friend ? " §ato friends!" : " §ato enemies!"));
				});
			} catch (Exception e) {
				Minecraft.getInstance().execute(() -> McCompat.chat("§cFailed to find player: " + e.getMessage()));
			}
		});
	}
}
