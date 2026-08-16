package io.github.christechs.pithelper.features;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.platform.InputConstants;

import io.github.christechs.pithelper.config.PitConfig;
import io.github.christechs.pithelper.data.HypixelGameMode;
import io.github.christechs.pithelper.mixin.KeyMappingAccessor;
import io.github.christechs.pithelper.utils.PlayerState;
import io.github.christechs.pithelper.utils.ServerState;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;

public class AfterDeathHandler {
	private boolean isBlocking = false;

	public void onTick(Minecraft mc) {
		if (mc.player == null || mc.screen != null) {
			return;
		}
		if (!PitConfig.general().blockMovementAfterDeath) {
			return;
		}
		if (PitConfig.general().onlyOnHypixel && (!ServerState.onHypixel || ServerState.currentGameMode != HypixelGameMode.PIT)) {
			return;
		}
		long timeSinceDeath = System.currentTimeMillis() - PlayerState.lastDeath;
		long blockDuration = PitConfig.general().blockMovementSeconds * 1000L;
		KeyMapping[] movementKeys = new KeyMapping[]{
			mc.options.keyUp, mc.options.keyDown, mc.options.keyLeft, mc.options.keyRight,
			mc.options.keyJump, mc.options.keyShift
		};
		if (timeSinceDeath < blockDuration) {
			isBlocking = true;
			for (KeyMapping key : movementKeys) {
				key.setDown(false);
			}
		} else if (isBlocking) {
			isBlocking = false;
			long handle = mc.getWindow().handle();
			for (KeyMapping key : movementKeys) {
				syncKey(key, handle);
			}
		}
	}

	private void syncKey(KeyMapping mapping, long handle) {
		InputConstants.Key key = ((KeyMappingAccessor) mapping).pithelper$getKey();
		boolean down;
		if (key.getType() == InputConstants.Type.MOUSE) {
			down = GLFW.glfwGetMouseButton(handle, key.getValue()) == GLFW.GLFW_PRESS;
		} else {
			down = GLFW.glfwGetKey(handle, key.getValue()) == GLFW.GLFW_PRESS;
		}
		mapping.setDown(down);
	}
}
