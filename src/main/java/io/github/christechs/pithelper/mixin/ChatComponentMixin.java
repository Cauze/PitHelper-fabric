package io.github.christechs.pithelper.mixin;

import io.github.christechs.pithelper.features.LobbyTracker;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ChatComponent.class)
public class ChatComponentMixin {
	@ModifyVariable(method = "addClientSystemMessage", at = @At("HEAD"), argsOnly = true)
	private Component pithelper$recolorClient(Component message) {
		return LobbyTracker.recolorMessage(message);
	}

	@ModifyVariable(method = "addServerSystemMessage", at = @At("HEAD"), argsOnly = true)
	private Component pithelper$recolorServer(Component message) {
		return LobbyTracker.recolorMessage(message);
	}

	@ModifyVariable(method = "addPlayerMessage", at = @At("HEAD"), argsOnly = true)
	private Component pithelper$recolorPlayer(Component message) {
		return LobbyTracker.recolorMessage(message);
	}
}
