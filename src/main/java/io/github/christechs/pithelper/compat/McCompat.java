package io.github.christechs.pithelper.compat;

import java.util.Locale;

import org.lwjgl.glfw.GLFW;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.multiplayer.ServerData;

public final class McCompat {
	private McCompat() {}

	public static Minecraft mc() {
		return Minecraft.getInstance();
	}

	public static Font font() {
		return mc().font;
	}

	public static int guiWidth() {
		return mc().getWindow().getGuiScaledWidth();
	}

	public static int guiHeight() {
		return mc().getWindow().getGuiScaledHeight();
	}

	public static void chat(String text) {
		if (mc().player != null) {
			mc().player.displayClientMessage(io.github.christechs.pithelper.utils.ChatUtil.legacy(text), false);
		}
	}

	public static boolean leftMouseDown() {
		long handle = mc().getWindow().handle();
		return GLFW.glfwGetMouseButton(handle, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
	}

	public static boolean isHypixelAddress() {
		ServerData server = mc().getCurrentServer();
		if (server == null) {
			return false;
		}
		String ip = server.ip.toLowerCase(Locale.ROOT);
		return ip.contains("hypixel.net") || ip.contains("hypixel.io");
	}

	public static String clipboard() {
		return mc().keyboardHandler.getClipboard();
	}

	public static void setClipboard(String text) {
		mc().keyboardHandler.setClipboard(text);
	}

	public static int argb(int r, int g, int b, int a) {
		return (a & 255) << 24 | (r & 255) << 16 | (g & 255) << 8 | (b & 255);
	}
}
