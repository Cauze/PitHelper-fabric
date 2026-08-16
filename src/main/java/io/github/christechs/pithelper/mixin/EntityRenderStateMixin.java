package io.github.christechs.pithelper.mixin;

import net.minecraft.client.renderer.entity.state.EntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(EntityRenderState.class)
public class EntityRenderStateMixin implements HighlightRenderState {
	@Unique
	private int pithelper$highlightColor = -1;

	@Override
	public void pithelper$setHighlightColor(int color) {
		this.pithelper$highlightColor = color;
	}

	@Override
	public int pithelper$getHighlightColor() {
		return this.pithelper$highlightColor;
	}
}
