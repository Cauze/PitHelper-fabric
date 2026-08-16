package io.github.christechs.pithelper.ui.screen;

import io.github.christechs.config.ConfigManager;
import io.github.christechs.pithelper.config.PitConfig;
import io.github.christechs.pithelper.data.EventFetcher;
import io.github.christechs.pithelper.ui.hud.EventOverlay;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public class EditOverlayScreen extends Screen {
	private DragMode currentMode = DragMode.NONE;
	private float startDragX = 0, startDragY = 0;
	private float startWidth = 0;
	private int startMouseX = 0, startMouseY = 0;

	public EditOverlayScreen() {
		super(Component.literal("Edit HUD"));
	}

	private OverlayBounds getBounds() {
		int scaledWidth = this.minecraft.getWindow().getGuiScaledWidth();
		int scaledHeight = this.minecraft.getWindow().getGuiScaledHeight();
		int activeCount = EventFetcher.activeEvent != null ? 1 : 0;
		int upcomingCount = EventFetcher.cachedEvents != null
			? Math.min(EventFetcher.cachedEvents.size(), PitConfig.hud().overlayEventCount)
			: 0;
		int totalRows = activeCount + upcomingCount;
		float scale = PitConfig.hud().overlayScale;
		OverlayBounds bounds = new OverlayBounds();
		bounds.height = (int) ((totalRows == 0 ? 30 : 38 + (totalRows * 22) - 6) * scale);
		bounds.width = (int) PitConfig.hud().overlayWidth;
		bounds.x = (int) (PitConfig.hud().overlayX * scaledWidth);
		bounds.y = (int) (PitConfig.hud().overlayY * scaledHeight);
		return bounds;
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		super.extractRenderState(graphics, mouseX, mouseY, partialTick);
		graphics.centeredText(this.font, "Click & Drag to move. Drag right edge to resize.", this.width / 2, 20, 0xFFFFFF);
		graphics.centeredText(this.font, "Scroll Mouse Wheel to scale HUD. Press ESC to save.", this.width / 2, 35, 0xAAAAAA);
		if (currentMode == DragMode.MOVING) {
			float newX = startDragX + ((mouseX - startMouseX) / (float) this.width);
			float newY = startDragY + ((mouseY - startMouseY) / (float) this.height);
			PitConfig.hud().overlayX = Math.max(0.0f, Math.min(1.0f, newX));
			PitConfig.hud().overlayY = Math.max(0.0f, Math.min(1.0f, newY));
		} else if (currentMode == DragMode.RESIZING) {
			PitConfig.hud().overlayWidth = Math.max(80f, startWidth + (mouseX - startMouseX));
		}
		EventOverlay.drawOverlayStatic(graphics, this.minecraft);
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
		OverlayBounds bounds = getBounds();
		int mouseX = (int) click.x();
		int mouseY = (int) click.y();
		if (Math.abs(mouseX - (bounds.x + bounds.width)) < 8 && mouseY >= bounds.y && mouseY <= bounds.y + bounds.height) {
			currentMode = DragMode.RESIZING;
			startWidth = PitConfig.hud().overlayWidth;
			startMouseX = mouseX;
			return true;
		}
		if (mouseX >= bounds.x && mouseX <= bounds.x + bounds.width && mouseY >= bounds.y && mouseY <= bounds.y + bounds.height) {
			currentMode = DragMode.MOVING;
			startDragX = PitConfig.hud().overlayX;
			startDragY = PitConfig.hud().overlayY;
			startMouseX = mouseX;
			startMouseY = mouseY;
			return true;
		}
		return super.mouseClicked(click, doubled);
	}

	@Override
	public boolean mouseReleased(MouseButtonEvent click) {
		currentMode = DragMode.NONE;
		return super.mouseReleased(click);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
		if (scrollY != 0) {
			PitConfig.hud().overlayScale += (scrollY > 0 ? 0.05f : -0.05f);
			PitConfig.hud().overlayScale = Math.max(0.5f, Math.min(2.0f, PitConfig.hud().overlayScale));
			return true;
		}
		return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
	}

	@Override
	public void onClose() {
		ConfigManager.save();
		this.minecraft.gui.setScreen(new PitHelperMenu());
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	private enum DragMode { NONE, MOVING, RESIZING }

	private static class OverlayBounds {
		int x, y, width, height;
	}
}
