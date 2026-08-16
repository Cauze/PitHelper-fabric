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
import io.github.christechs.pithelper.config.PitConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public class ClayRenderer {

	public static void draw(LayoutResults results, GuiGraphics graphics) {
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
				case TEXT -> renderText(graphics, font, cmd, box);
				case CUSTOM -> renderCustomItem(graphics, cmd, x1, y1);
				case SCISSOR_START -> graphics.enableScissor(x1, y1, x2, y2);
				case SCISSOR_END -> graphics.disableScissor();
			}
		}
	}

	private static void renderRectangle(GuiGraphics graphics, RenderCommand cmd, int x1, int y1, int x2, int y2) {
		Color bg = cmd.renderData.backgroundColor;
		if (bg == null || bg.a <= 0) {
			return;
		}
		CornerRadius cr = cmd.renderData.cornerRadius;
		if (cr != null && (cr.topLeft > 0 || cr.topRight > 0 || cr.bottomLeft > 0 || cr.bottomRight > 0)) {
			drawRoundedRect(graphics, cmd.boundingBox.x, cmd.boundingBox.y, cmd.boundingBox.width, cmd.boundingBox.height, cr, bg);
			return;
		}
		graphics.fill(x1, y1, x2, y2, colorToInt(bg));
	}

	private static void drawRoundedRect(GuiGraphics graphics, float x, float y, float w, float h, CornerRadius cr, Color color) {
		int col = colorToInt(color);
		float maxR = Math.min(w / 2f, h / 2f);
		float rtl = Math.max(0, Math.min(cr.topLeft, maxR));
		float rtr = Math.max(0, Math.min(cr.topRight, maxR));
		float rbl = Math.max(0, Math.min(cr.bottomLeft, maxR));
		float rbr = Math.max(0, Math.min(cr.bottomRight, maxR));
		int y0 = Math.round(y);
		int y1 = Math.round(y + h);
		for (int py = y0; py < y1; py++) {
			float fy = (py + 0.5f) - y;
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
			int x0 = Math.round(x + left);
			int x2 = Math.round(x + right);
			if (x2 > x0) {
				graphics.fill(x0, py, x2, py + 1, col);
			}
		}
	}

	private static void renderBorder(GuiGraphics graphics, RenderCommand cmd, BoundingBox box) {
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

	private static void renderImage(GuiGraphics graphics, RenderCommand cmd, int x1, int y1, int x2, int y2) {
		if (!(cmd.renderData.imageData instanceof Identifier texture)) {
			return;
		}
		int w = Math.max(1, x2 - x1);
		int h = Math.max(1, y2 - y1);
		graphics.blit(RenderPipelines.GUI_TEXTURED, texture, x1, y1, 0f, 0f, w, h, w, h);
	}

	private static void renderText(GuiGraphics graphics, Font font, RenderCommand cmd, BoundingBox box) {
		CharSequence fullText = cmd.renderData.text;
		int start = cmd.renderData.textStart;
		int length = cmd.renderData.textLength;
		String lineText = fullText.subSequence(start, start + length).toString();
		float textScale = cmd.renderData.fontSize > 0 ? cmd.renderData.fontSize : 1.0f;
		int colorInt = colorToInt(cmd.renderData.textColor);
		graphics.pose().pushMatrix();
		graphics.pose().translate(box.x, box.y);
		graphics.pose().scale(textScale, textScale);
		graphics.drawString(font, lineText, 0, 0, colorInt, PitConfig.hud().textDropShadow);
		graphics.pose().popMatrix();
	}

	private static void renderCustomItem(GuiGraphics graphics, RenderCommand cmd, int x, int y) {
		if (cmd.renderData.customData instanceof ItemStack item) {
			graphics.renderItem(item, x, y);
		}
	}

	private static int colorToInt(Color color) {
		return ((int) color.a << 24) | ((int) color.r << 16) | ((int) color.g << 8) | (int) color.b;
	}
}
