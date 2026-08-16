/*
 * PitHelper - A Hypixel The Pit Helper Mod.
 * Copyright (C) 2026 Christian Steenkamp
 */

package io.github.christechs.pithelper.ui.base;

import io.github.christechs.clayj.ClayJ;
import io.github.christechs.clayj.LayoutResults;
import io.github.christechs.clayj.math.Dimensions;
import io.github.christechs.clayj.math.Vector2;
import io.github.christechs.pithelper.compat.McCompat;
import io.github.christechs.pithelper.ui.ClayRenderer;
import io.github.christechs.pithelper.ui.components.ClayComponents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public abstract class ClayScreen extends Screen {

	protected boolean mouseClickedThisFrame = false;
	protected ItemStack hoveredItemTooltip = ItemStack.EMPTY;
	private boolean initialized = false;
	private long lastFrameTime = System.currentTimeMillis();
	private float scrollMomentum = 0f;
	protected Minecraft mc;

	protected ClayScreen() {
		super(Component.literal("PitHelper"));
		this.mc = Minecraft.getInstance();
	}

	@Override
	protected void init() {
		super.init();
		this.mc = Minecraft.getInstance();
		if (!initialized) {
			if (ClayJ.getContext() == null) {
				ClayJ.initialize(2048, 2048, new Dimensions(this.width, this.height));
				ClayJ.setMeasureTextFunction((text, start, len, config, outDimensions) -> {
					String sub = text.subSequence(start, start + len).toString();
					int scale = config.fontSize > 0 ? config.fontSize : 1;
					outDimensions.set(this.font.width(sub) * scale, this.font.lineHeight * scale);
				});
			}
			initialized = true;
		}
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		super.extractRenderState(graphics, mouseX, mouseY, partialTick);

		long currentTime = System.currentTimeMillis();
		float deltaTime = (currentTime - lastFrameTime) / 1000f;
		lastFrameTime = currentTime;

		float scrollDeltaY = scrollMomentum;
		scrollMomentum *= 0.75f;
		if (Math.abs(scrollMomentum) < 0.1f) {
			scrollMomentum = 0f;
		}

		ClayComponents.updateInputState(mouseX, mouseY, this.mouseClickedThisFrame);

		ClayJ.setLayoutDimensions(this.width, this.height);
		ClayJ.setPointerState(new Vector2(mouseX, mouseY), McCompat.leftMouseDown());
		ClayJ.updateScrollContainers(true, new Vector2(0, scrollDeltaY / 10.0f), deltaTime);

		ClayJ.beginLayout();
		buildLayout(mouseX, mouseY, deltaTime);
		LayoutResults results = ClayJ.endLayout();
		ClayRenderer.draw(results, graphics, false);

		if (hoveredItemTooltip != null && !hoveredItemTooltip.isEmpty()) {
			graphics.setTooltipForNextFrame(this.font, hoveredItemTooltip, mouseX, mouseY);
		}

		this.mouseClickedThisFrame = false;
	}

	protected abstract void buildLayout(int mouseX, int mouseY, float deltaTime);

	@Override
	public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
		if (click.button() == 0) {
			this.mouseClickedThisFrame = true;
		}
		return super.mouseClicked(click, doubled);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
		if (scrollY != 0) {
			scrollMomentum += Math.signum(scrollY) * 15f;
			return true;
		}
		return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
	}

	@Override
	public boolean keyPressed(KeyEvent event) {
		ClayComponents.onKeyPressed(event);
		return super.keyPressed(event);
	}

	@Override
	public boolean charTyped(CharacterEvent event) {
		ClayComponents.onCharTyped((char) event.codepoint());
		return super.charTyped(event);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}
}
