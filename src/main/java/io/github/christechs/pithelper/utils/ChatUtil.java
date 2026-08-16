package io.github.christechs.pithelper.utils;

import java.util.Locale;
import java.util.Optional;

import io.github.christechs.pithelper.compat.McCompat;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

public class ChatUtil {
	public static void simulateChat(String text) {
		McCompat.chat(text);
	}

	public static MutableComponent legacy(String text) {
		MutableComponent out = Component.empty();
		if (text == null || text.isEmpty()) {
			return out;
		}
		Style style = Style.EMPTY.withItalic(false);
		StringBuilder buf = new StringBuilder();
		for (int i = 0; i < text.length(); i++) {
			char c = text.charAt(i);
			if (c == '§' && i + 1 < text.length()) {
				flush(out, buf, style);
				char code = Character.toLowerCase(text.charAt(++i));
				if (code == 'r') {
					style = Style.EMPTY.withItalic(false);
					continue;
				}
				ChatFormatting formatting = ChatFormatting.getByCode(code);
				if (formatting == null) {
					continue;
				}
				if (isFormat(formatting)) {
					style = style.applyFormat(formatting);
				} else {
					style = Style.EMPTY.withItalic(false).applyFormat(formatting);
				}
			} else {
				buf.append(c);
			}
		}
		flush(out, buf, style);
		return out;
	}

	public static String toLegacy(Component component) {
		StringBuilder sb = new StringBuilder();
		component.visit((style, text) -> {
			sb.append(styleToLegacy(style));
			sb.append(text);
			return Optional.empty();
		}, Style.EMPTY);
		return sb.toString();
	}

	private static void flush(MutableComponent out, StringBuilder buf, Style style) {
		if (buf.length() == 0) {
			return;
		}
		out.append(Component.literal(buf.toString()).withStyle(style));
		buf.setLength(0);
	}

	private static String styleToLegacy(Style style) {
		StringBuilder sb = new StringBuilder("§r");
		if (style.getColor() != null) {
			ChatFormatting named = namedColor(style.getColor().serialize());
			if (named != null) {
				String code = named.toString();
				if (code.startsWith("§")) {
					sb.append(code);
				} else {
					sb.append('§').append(code.charAt(code.length() - 1));
				}
			}
		}
		if (style.isObfuscated()) sb.append("§k");
		if (style.isBold()) sb.append("§l");
		if (style.isStrikethrough()) sb.append("§m");
		if (style.isUnderlined()) sb.append("§n");
		if (style.isItalic()) sb.append("§o");
		return sb.toString();
	}

	private static boolean isFormat(ChatFormatting formatting) {
		return formatting == ChatFormatting.OBFUSCATED
			|| formatting == ChatFormatting.BOLD
			|| formatting == ChatFormatting.STRIKETHROUGH
			|| formatting == ChatFormatting.UNDERLINE
			|| formatting == ChatFormatting.ITALIC;
	}

	private static ChatFormatting namedColor(String name) {
		if (name == null || name.isEmpty()) {
			return null;
		}
		try {
			return ChatFormatting.valueOf(name.toUpperCase(Locale.ROOT));
		} catch (IllegalArgumentException ignored) {
			return null;
		}
	}
}
