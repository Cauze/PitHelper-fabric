/*
 * PitHelper - A Hypixel The Pit Helper Mod.
 * Copyright (C) 2026 Christian Steenkamp
 */

package io.github.christechs.pithelper.ui;

import io.github.christechs.clayj.LayoutResults;
import io.github.christechs.clayj.core.RenderCommand;
import io.github.christechs.clayj.math.BoundingBox;
import io.github.christechs.clayj.math.Color;
import io.github.christechs.clayj.math.CornerRadius;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;

public class ClayRenderer {

	public static void draw(LayoutResults results, GuiGraphicsExtractor graphics, boolean textShadow) {
		Minecraft mc = Minecraft.getInstance();
		Font font = mc.font;

		for (int i = 0; i < results.length(); i++) {
			RenderCommand cmd = results.get(i);
			BoundingBox box = cmd.boundingBox;
			int x1 = Math.round(box.x);
			int y1 = Math.round(box.y);
			int x2 = Math.round(box.x + box.width);
			int y2 = Math.round(box.y + box.height);

			switch (cmd.commandType) {
				case RECTANGLE -> renderRectangle(graphics, cmd, x1, y1, x2, y2);
				case BORDER -> renderBorder(graphics, cmd, box);
				case IMAGE -> renderImage(graphics, cmd, x1, y1, x2, y2);
				case TEXT -> renderText(graphics, font, cmd, box, textShadow);
				case CUSTOM -> renderCustomItem(graphics, cmd, x1, y1);
				case SCISSOR_START -> graphics.enableScissor(x1, y1, x2, y2);
				case SCISSOR_END -> graphics.disableScissor();
			}
		}
	}

	private static void renderRectangle(GuiGraphicsExtractor graphics, RenderCommand cmd, int x1, int y1, int x2, int y2) {
		Color bg = cmd.renderData.backgroundColor;
		if (bg == null || bg.a <= 0) {
			return;
		}
		CornerRadius cr = cmd.renderData.cornerRadius;
		if (cr != null && (cr.topLeft > 0 || cr.topRight > 0 || cr.bottomLeft > 0 || cr.bottomRight > 0)) {
			drawRoundedRect(graphics, x1, y1, x2, y2, cr, bg);
			return;
		}
		graphics.fill(x1, y1, x2, y2, colorToInt(bg));
	}

	private static void drawRoundedRect(GuiGraphicsExtractor graphics, int x, int y, int x2, int y2, CornerRadius cr, Color color) {
		int col = colorToInt(color);
		int w = Math.max(1, x2 - x);
		int h = Math.max(1, y2 - y);
		int scale = Math.max(1, (int) Math.round(Minecraft.getInstance().getWindow().getGuiScale()));
		if (scale <= 1) {
			fillRoundedScanlines(graphics, x, y, w, h, cr, col);
			return;
		}
		graphics.pose().pushMatrix();
		graphics.pose().scale(1f / scale, 1f / scale);
		fillRoundedScanlines(graphics, x * scale, y * scale, w * scale, h * scale, scaledRadius(cr, scale, w, h), col);
		graphics.pose().popMatrix();
	}

	private static CornerRadius scaledRadius(CornerRadius cr, int scale, int w, int h) {
		float maxR = Math.min(w / 2f, h / 2f) * scale;
		return new CornerRadius(
			Math.min(cr.topLeft * scale, maxR),
			Math.min(cr.topRight * scale, maxR),
			Math.min(cr.bottomLeft * scale, maxR),
			Math.min(cr.bottomRight * scale, maxR)
		);
	}

	private static void fillRoundedScanlines(GuiGraphicsExtractor graphics, int x, int y, int w, int h, CornerRadius cr, int col) {
		float maxR = Math.min(w / 2f, h / 2f);
		float rtl = Math.max(0, Math.min(cr.topLeft, maxR));
		float rtr = Math.max(0, Math.min(cr.topRight, maxR));
		float rbl = Math.max(0, Math.min(cr.bottomLeft, maxR));
		float rbr = Math.max(0, Math.min(cr.bottomRight, maxR));
		for (int row = 0; row < h; row++) {
			float fy = row + 0.5f;
			float left = 0f;
			float right = w;
			if (fy < rtl) {
				float dy = rtl - fy;
				left = Math.max(left, rtl - (float) Math.sqrt(Math.max(0, rtl * rtl - dy * dy)));
			}
			if (fy > h - rbl) {
				float dy = fy - (h - rbl);
				left = Math.max(left, rbl - (float) Math.sqrt(Math.max(0, rbl * rbl - dy * dy)));
			}
			if (fy < rtr) {
				float dy = rtr - fy;
				right = Math.min(right, w - (rtr - (float) Math.sqrt(Math.max(0, rtr * rtr - dy * dy))));
			}
			if (fy > h - rbr) {
				float dy = fy - (h - rbr);
				right = Math.min(right, w - (rbr - (float) Math.sqrt(Math.max(0, rbr * rbr - dy * dy))));
			}
			int rowX0 = x + Math.round(left);
			int rowX1 = x + Math.round(right);
			if (rowX1 > rowX0) {
				graphics.fill(rowX0, y + row, rowX1, y + row + 1, col);
			}
		}
	}

	private static void renderBorder(GuiGraphicsExtractor graphics, RenderCommand cmd, BoundingBox box) {
		Color borderColor = cmd.renderData.borderColor;
		if (borderColor == null || borderColor.a <= 0) {
			return;
		}
		int color = colorToInt(borderColor);
		int x = Math.round(box.x);
		int y = Math.round(box.y);
		int w = Math.round(box.width);
		int h = Math.round(box.height);
		int top = Math.round(cmd.renderData.borderWidth.top);
		int bottom = Math.round(cmd.renderData.borderWidth.bottom);
		int left = Math.round(cmd.renderData.borderWidth.left);
		int right = Math.round(cmd.renderData.borderWidth.right);
		if (top > 0) graphics.fill(x, y, x + w, y + top, color);
		if (bottom > 0) graphics.fill(x, y + h - bottom, x + w, y + h, color);
		if (left > 0) graphics.fill(x, y, x + left, y + h, color);
		if (right > 0) graphics.fill(x + w - right, y, x + w, y + h, color);
	}

	private static void renderImage(GuiGraphicsExtractor graphics, RenderCommand cmd, int x1, int y1, int x2, int y2) {
		if (!(cmd.renderData.imageData instanceof Identifier texture)) {
			return;
		}
		int w = Math.max(1, x2 - x1);
		int h = Math.max(1, y2 - y1);
		AbstractTexture gpuTexture = Minecraft.getInstance().getTextureManager().getTexture(texture);
		if (gpuTexture == null || gpuTexture.getTextureView() == null) {
			return;
		}
		// 26.2 blit UVs are (u0, u1, v0, v1), not (u0, v0, u1, v1).
		graphics.blit(
			gpuTexture.getTextureView(),
			RenderSystem.getSamplerCache().getClampToEdge(FilterMode.NEAREST),
			x1, y1, x1 + w, y1 + h,
			0f, 1f, 0f, 1f
		);
	}

	private static void renderText(GuiGraphicsExtractor graphics, Font font, RenderCommand cmd, BoundingBox box, boolean textShadow) {
		CharSequence fullText = cmd.renderData.text;
		int start = cmd.renderData.textStart;
		int length = cmd.renderData.textLength;
		String lineText = fullText.subSequence(start, start + length).toString();
		float textScale = cmd.renderData.fontSize > 0 ? cmd.renderData.fontSize : 1.0f;
		int colorInt = colorToInt(cmd.renderData.textColor);
		int x = Math.round(box.x);
		int y = Math.round(box.y);
		if (textScale == 1.0f) {
			graphics.text(font, lineText, x, y, colorInt, textShadow);
			return;
		}
		graphics.pose().pushMatrix();
		graphics.pose().translate(x, y);
		graphics.pose().scale(textScale, textScale);
		graphics.text(font, lineText, 0, 0, colorInt, textShadow);
		graphics.pose().popMatrix();
	}

	private static void renderCustomItem(GuiGraphicsExtractor graphics, RenderCommand cmd, int x, int y) {
		if (cmd.renderData.customData instanceof ItemStack item) {
			graphics.item(item, x, y);
		}
	}

	private static int colorToInt(Color color) {
		return ((int) color.a << 24) | ((int) color.r << 16) | ((int) color.g << 8) | (int) color.b;
	}
}
