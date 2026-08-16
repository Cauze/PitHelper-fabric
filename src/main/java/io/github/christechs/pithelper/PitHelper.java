/*
 * PitHelper - A Hypixel The Pit Helper Mod.
 * Copyright (C) 2026 Christian Steenkamp
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 */

package io.github.christechs.pithelper;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.platform.InputConstants;

import io.github.christechs.config.ConfigManager;
import io.github.christechs.pithelper.commands.PitCommands;
import io.github.christechs.pithelper.config.PitConfig;
import io.github.christechs.pithelper.features.AfterDeathHandler;
import io.github.christechs.pithelper.features.AutoSpawnHandler;
import io.github.christechs.pithelper.features.LobbyTracker;
import io.github.christechs.pithelper.features.NotificationHandler;
import io.github.christechs.pithelper.features.QuickMathsHandler;
import io.github.christechs.pithelper.ui.hud.EventOverlay;
import io.github.christechs.pithelper.utils.PlayerState;
import io.github.christechs.pithelper.utils.ServerState;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fabricmc.loader.api.FabricLoader;

public class PitHelper implements ClientModInitializer {

	public static final String MOD_ID = "pithelper";
	public static final Logger PH_LOGGER = LogManager.getLogger("PitHelper");

	public static KeyMapping.Category KEY_CATEGORY;
	public static KeyMapping autoSpawnKey;

	private final NotificationHandler notifications = new NotificationHandler();
	private final QuickMathsHandler quickMaths = new QuickMathsHandler();
	private final AfterDeathHandler afterDeath = new AfterDeathHandler();
	private final AutoSpawnHandler autoSpawn = new AutoSpawnHandler();
	private final LobbyTracker lobbyTracker = new LobbyTracker();
	private final PlayerState playerState = new PlayerState();

	@Override
	public void onInitializeClient() {
		File configFile = new File(FabricLoader.getInstance().getConfigDir().toFile(), "pithelper.json");
		ConfigManager.init(configFile);
		PitConfig.registerDynamicConfigs(configFile);

		KEY_CATEGORY = KeyMapping.Category.register(Identifier.fromNamespaceAndPath(MOD_ID, "keys"));
		autoSpawnKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
			"key.pithelper.auto_spawn",
			InputConstants.Type.KEYSYM,
			GLFW.GLFW_KEY_UNKNOWN,
			KEY_CATEGORY
		));

		PitCommands.register();

		HudElementRegistry.attachElementAfter(
			VanillaHudElements.SCOREBOARD,
			Identifier.fromNamespaceAndPath(MOD_ID, "overlay"),
			EventOverlay::renderHud
		);

		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> ServerState.onJoin());
		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> ServerState.onDisconnect());

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			ServerState.tick(client);
			notifications.onTick(client);
			afterDeath.onTick(client);
			autoSpawn.onTick(client);
			lobbyTracker.onTick(client);
		});

		ClientReceiveMessageEvents.ALLOW_GAME.register((message, overlay) -> {
			if (overlay) {
				return true;
			}
			return !ServerState.tryConsumeLocraw(message.getString());
		});

		ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
			if (overlay) {
				return;
			}
			String raw = message.getString();
			playerState.onChat(raw);
			quickMaths.onChat(raw);
			autoSpawn.onChat(raw);
			lobbyTracker.onChat(raw);
		});

		PH_LOGGER.info("PitHelper Fabric loaded");
	}

	public static Minecraft mc() {
		return Minecraft.getInstance();
	}
}
