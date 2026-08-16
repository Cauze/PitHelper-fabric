package io.github.christechs.pithelper.mixin;

import io.github.christechs.pithelper.utils.PlayerHighlightUtil;
import net.minecraft.client.renderer.entity.layers.EquipmentLayerRenderer;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EquipmentLayerRenderer.class)
public class EquipmentLayerRendererMixin {
	@Inject(method = "getColorForLayer", at = @At("RETURN"), cancellable = true)
	private static void pithelper$tintArmor(EquipmentClientInfo.Layer layer, int color, CallbackInfoReturnable<Integer> cir) {
		int highlight = PlayerHighlightUtil.current();
		if (highlight != -1) {
			cir.setReturnValue(0xFF000000 | highlight);
		}
	}
}
