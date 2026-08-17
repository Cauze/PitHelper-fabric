package io.github.christechs.pithelper.mixin;

import io.github.christechs.pithelper.render.HighlightRenderState;
import io.github.christechs.pithelper.utils.PlayerHighlightUtil;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import com.mojang.blaze3d.vertex.PoseStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin {

	@Inject(
		method = "extractRenderState(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;F)V",
		at = @At("HEAD")
	)
	private void pithelper$clearHighlight(LivingEntity entity, LivingEntityRenderState state, float tickDelta, CallbackInfo ci) {
		((HighlightRenderState) state).pithelper$setHighlightColor(-1);
	}

	@Inject(
		method = "extractRenderState(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;F)V",
		at = @At("TAIL")
	)
	private void pithelper$extractHighlight(LivingEntity entity, LivingEntityRenderState state, float tickDelta, CallbackInfo ci) {
		if (entity instanceof Player player) {
			((HighlightRenderState) state).pithelper$setHighlightColor(PlayerHighlightUtil.getHighlightColor(player));
		}
	}

	@Inject(method = "getModelTint", at = @At("RETURN"), cancellable = true)
	private void pithelper$tintModel(LivingEntityRenderState state, CallbackInfoReturnable<Integer> cir) {
		int highlight = ((HighlightRenderState) state).pithelper$getHighlightColor();
		if (highlight != -1) {
			cir.setReturnValue(0xFF000000 | highlight);
		}
	}

	@Inject(
		method = "submit(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/CameraRenderState;)V",
		at = @At("HEAD")
	)
	private void pithelper$beginTint(LivingEntityRenderState state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera, CallbackInfo ci) {
		PlayerHighlightUtil.push(state instanceof HighlightRenderState highlight ? highlight.pithelper$getHighlightColor() : -1);
	}

	@Inject(
		method = "submit(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/CameraRenderState;)V",
		at = @At("RETURN")
	)
	private void pithelper$endTint(LivingEntityRenderState state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera, CallbackInfo ci) {
		PlayerHighlightUtil.pop();
	}
}
