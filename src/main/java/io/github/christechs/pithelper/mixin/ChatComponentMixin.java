package io.github.christechs.pithelper.mixin;

import io.github.christechs.pithelper.features.LobbyTracker;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.multiplayer.chat.GuiMessageTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MessageSignature;
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

	@ModifyVariable(
		method = "addPlayerMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;Lnet/minecraft/client/multiplayer/chat/GuiMessageTag;)V",
		at = @At("HEAD"),
		argsOnly = true
	)
	private Component pithelper$recolorPlayer(Component message, MessageSignature signature, GuiMessageTag tag) {
		return LobbyTracker.recolorMessage(message);
	}
}
