/*
 * PitHelper - A Hypixel The Pit Helper Mod.
 * Copyright (C) 2026 Christian Steenkamp
 */

package io.github.christechs.pithelper.ui.hud;

import java.util.concurrent.TimeUnit;

import io.github.christechs.clayj.ClayJ;
import io.github.christechs.clayj.LayoutResults;
import io.github.christechs.clayj.config.FloatingConfigBuilder;
import io.github.christechs.clayj.config.ImageConfigBuilder;
import io.github.christechs.clayj.enums.LayoutAlignmentY;
import io.github.christechs.clayj.math.Dimensions;
import io.github.christechs.clayj.math.Vector2;
import io.github.christechs.pithelper.config.PitConfig;
import io.github.christechs.pithelper.data.EventFetcher;
import io.github.christechs.pithelper.data.PitEvent;
import io.github.christechs.pithelper.features.NotificationManager;
import io.github.christechs.pithelper.ui.ClayRenderer;
import io.github.christechs.pithelper.ui.base.ClayScreen;
import io.github.christechs.pithelper.ui.screen.EditOverlayScreen;
import io.github.christechs.pithelper.ui.utils.IconCache;
import io.github.christechs.pithelper.utils.ServerState;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

import static io.github.christechs.clayj.ClayJ.decl;
import static io.github.christechs.clayj.ClayJ.el;
import static io.github.christechs.clayj.ClayJ.layout;
import static io.github.christechs.clayj.ClayJ.text;
import static io.github.christechs.clayj.ClayJ.txt;
import static io.github.christechs.clayj.enums.AttachToElement.ROOT;
import static io.github.christechs.clayj.enums.LayoutAlignmentX.LEFT;
import static io.github.christechs.clayj.enums.LayoutDirection.LEFT_TO_RIGHT;
import static io.github.christechs.clayj.enums.LayoutDirection.TOP_TO_BOTTOM;
import static io.github.christechs.clayj.enums.SizingType.FIT;
import static io.github.christechs.clayj.enums.SizingType.FIXED;
import static io.github.christechs.clayj.enums.SizingType.GROW;

public class EventOverlay {

	public static boolean isClayMenuOpen(Minecraft mc) {
		return mc.screen instanceof ClayScreen;
	}

	public static void renderHud(GuiGraphics graphics, DeltaTracker tickCounter) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.options.hideGui) {
			return;
		}
		if (PitConfig.general().onlyOnHypixel && !ServerState.onHypixel) {
			return;
		}
		if (ServerState.onHypixel && ServerState.currentGameMode != null) {
			Boolean toggle = PitConfig.gamemodes().toggles.getOrDefault(ServerState.currentGameMode, false);
			if (!toggle) {
				return;
			}
		}
		if (isClayMenuOpen(mc) || mc.screen instanceof EditOverlayScreen) {
			return;
		}
		if (!PitConfig.hud().overlayEnabled && NotificationManager.activeNotifications.isEmpty()) {
			return;
		}
		if (EventFetcher.cachedEvents == null && !EventFetcher.isFetching) {
			EventFetcher.fetchAsync();
			return;
		}
		drawOverlayStatic(graphics, mc);
	}

	public static void drawOverlayStatic(GuiGraphics graphics, Minecraft mc) {
		int scaledWidth = mc.getWindow().getGuiScaledWidth();
		int scaledHeight = mc.getWindow().getGuiScaledHeight();

		if (ClayJ.getContext() == null) {
			ClayJ.initialize(2048, 2048, new Dimensions(scaledWidth, scaledHeight));
			ClayJ.setMeasureTextFunction((text, start, len, config, outDimensions) -> {
				String sub = text.subSequence(start, start + len).toString();
				float scale = config.fontSize > 0 ? (float) config.fontSize : 1.0f;
				outDimensions.set(mc.font.width(sub) * scale, mc.font.lineHeight * scale);
			});
		}

		boolean isEditScreen = mc.screen instanceof EditOverlayScreen;
		if ((PitConfig.hud().overlayEnabled || isEditScreen) && !isClayMenuOpen(mc)) {
			drawHUD(graphics, mc, scaledWidth, scaledHeight);
		}
		if (!NotificationManager.activeNotifications.isEmpty()) {
			drawNotification(graphics, mc, scaledWidth, scaledHeight);
		}
	}

	private static void drawHUD(GuiGraphics graphics, Minecraft mc, int scaledWidth, int scaledHeight) {
		float scale = PitConfig.hud().overlayScale;
		ClayJ.setLayoutDimensions(scaledWidth / scale, scaledHeight / scale);
		ClayJ.setPointerState(new Vector2(-1, -1), false);
		ClayJ.beginLayout();

		float renderX = PitConfig.hud().overlayX * (scaledWidth / scale);
		float renderY = PitConfig.hud().overlayY * (scaledHeight / scale);
		float renderW = PitConfig.hud().overlayWidth / scale;

		el(decl().floating(new FloatingConfigBuilder()
				.attachTo(ROOT)
				.offset(renderX, renderY))
			.bg(20, 20, 22, 180)
			.radius(8).layout(layout()
				.sizing(FIXED, renderW, FIT, 0)
				.dir(TOP_TO_BOTTOM)
				.padding(8, 8).gap(6)),
			() -> {
				long currentTime = System.currentTimeMillis();
				if (EventFetcher.activeEvent != null) {
					PitEvent ae = EventFetcher.activeEvent;
					long actualEnd = ae.timestamp + ae.eventType.startOffset + ae.eventType.duration;
					if (currentTime < actualEnd) {
						drawUnifiedRow(ae, actualEnd - currentTime, true);
					}
				}
				if (EventFetcher.cachedEvents != null) {
					int count = 0;
					for (PitEvent e : EventFetcher.cachedEvents) {
						long actualStart = e.timestamp + e.eventType.startOffset;
						if (actualStart < currentTime) {
							continue;
						}
						if (count >= PitConfig.hud().overlayEventCount) {
							break;
						}
						drawUnifiedRow(e, actualStart - currentTime, false);
						count++;
					}
				}
				if (PitConfig.hud().showEventDataCredit) {
					el(decl().layout(layout().sizing(GROW, 0, FIXED, 2)), () -> {});
					text("Credit to BrookeAFK for event data.", txt().size(1).color(120, 120, 120, 255));
				}
			});

		LayoutResults results = ClayJ.endLayout();
		if (scale == 1.0f) {
			ClayRenderer.draw(results, graphics, PitConfig.hud().textDropShadow);
			return;
		}
		graphics.pose().pushMatrix();
		graphics.pose().scale(scale, scale);
		ClayRenderer.draw(results, graphics, PitConfig.hud().textDropShadow);
		graphics.pose().popMatrix();
	}

	private static void drawNotification(GuiGraphics graphics, Minecraft mc, int scaledWidth, int scaledHeight) {
		ClayJ.setLayoutDimensions(scaledWidth, scaledHeight);
		ClayJ.setPointerState(new Vector2(-1, -1), false);
		ClayJ.beginLayout();

		int index = 0;
		for (NotificationManager.VisualNotif notif : NotificationManager.activeNotifications) {
			float yPos = 20 + (index * 45);
			el(decl().floating(new FloatingConfigBuilder()
					.attachTo(ROOT)
					.offset(scaledWidth / 2f - 100, yPos))
				.bg(20, 20, 22, 220)
				.radius(8)
				.layout(layout()
					.sizing(FIXED, 200, FIT, 0)
					.dir(TOP_TO_BOTTOM)
					.padding(10, 10).gap(4)), () -> {
				text("Event Starting Soon!", txt().size(1).color(255, 170, 0, 255));
				if (notif.event != null) {
					long startCountdown = notif.event.timestamp + notif.event.eventType.startOffset;
					drawUnifiedRow(notif.event, startCountdown - System.currentTimeMillis(), false);
				} else {
					text(notif.fallbackText, txt().size(1).color(255, 255, 255, 255));
				}
			});
			index++;
		}
		ClayRenderer.draw(ClayJ.endLayout(), graphics, PitConfig.hud().textDropShadow);
	}

	private static void drawUnifiedRow(PitEvent e, long timeDiff, boolean isActive) {
		el(decl().layout(layout()
				.sizing(GROW, 0, FIT, 0)
				.dir(LEFT_TO_RIGHT)
				.align(LEFT, LayoutAlignmentY.CENTER)
				.gap(6)),
			() -> {
				el(decl().image(new ImageConfigBuilder()
						.data(IconCache.get(e.event))
						.sourceDim(16, 16)).layout(layout()
						.sizing(FIXED, 16, FIXED, 16)),
					() -> {});
				text((isActive ? "§a" : "§f") + e.event, txt().size(1).color(255, 255, 255, 255));
				el(decl().layout(layout().sizing(GROW, 0, FIXED, 0)), () -> {});
				text(formatTimeVerbose(timeDiff), txt().size(1)
					.color(isActive ? 85 : 170, 255, isActive ? 85 : 170, 255));
			});
	}

	public static String formatTimeVerbose(long ms) {
		if (ms <= 0) {
			return "0s";
		}
		long days = TimeUnit.MILLISECONDS.toDays(ms);
		long hours = TimeUnit.MILLISECONDS.toHours(ms) % 24;
		long minutes = TimeUnit.MILLISECONDS.toMinutes(ms) % 60;
		long seconds = TimeUnit.MILLISECONDS.toSeconds(ms) % 60;
		StringBuilder sb = new StringBuilder();
		if (days > 0) sb.append(days).append("d ");
		if (hours > 0) sb.append(hours).append("h ");
		if (minutes > 0) sb.append(minutes).append("m ");
		sb.append(seconds).append("s");
		return sb.toString().trim();
	}
}
