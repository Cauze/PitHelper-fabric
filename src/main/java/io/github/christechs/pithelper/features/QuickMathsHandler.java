package io.github.christechs.pithelper.features;

import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import net.objecthunter.exp4j.ExpressionBuilder;

import io.github.christechs.pithelper.compat.McCompat;
import io.github.christechs.pithelper.config.PitConfig;
import io.github.christechs.pithelper.data.HypixelGameMode;
import io.github.christechs.pithelper.utils.ServerState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;

public class QuickMathsHandler {
	private static final Pattern STRIP_COLOR_PATTERN = Pattern.compile("(?i)§[0-9A-FK-OR]");
	private static final Logger LOGGER = LogManager.getLogger("PitHelper");

	public void onChat(String raw) {
		if (PitConfig.general().onlyOnHypixel && (!ServerState.onHypixel || ServerState.currentGameMode != HypixelGameMode.PIT)) {
			return;
		}
		String message = STRIP_COLOR_PATTERN.matcher(raw.toLowerCase()).replaceAll("");
		if (!message.startsWith("quick maths! solve: ")) {
			return;
		}
		String problem = message.substring(message.indexOf("solve: ") + 7).replace(" ", "").replace("x", "*");
		try {
			double rawResult = new ExpressionBuilder(problem).build().evaluate();
			String result = (rawResult == Math.floor(rawResult)) ? String.valueOf((long) rawResult) : String.valueOf(rawResult);
			handleAnswerFound(result);
		} catch (Exception e) {
			LOGGER.error("Failed to solve Quick Maths equation: {}", problem);
		}
	}

	private void handleAnswerFound(String result) {
		Minecraft mc = Minecraft.getInstance();
		if (PitConfig.general().quickMathsClipboard) {
			McCompat.setClipboard(result);
		}
		String alert = "§6§l[PitHelper] §eQuick Math Answer: §a§l" + result;
		if (PitConfig.general().quickMathsClipboard) {
			alert += " §7(Copied to Clipboard)";
		}
		McCompat.chat(alert);
		if (PitConfig.general().quickMathsAutoOpenChat) {
			mc.execute(() -> mc.setScreen(new ChatScreen(result, false)));
		}
	}
}
