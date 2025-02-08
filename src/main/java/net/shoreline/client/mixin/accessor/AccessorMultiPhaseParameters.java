package net.shoreline.client.mixin.accessor;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderPhase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(RenderLayer.MultiPhaseParameters.class)
public interface AccessorMultiPhaseParameters
{
    @Accessor("outlineMode")
    RenderLayer.OutlineMode hookGetOutlineMode();

    @Accessor("texture")
    RenderPhase.TextureBase hookGetTexture();
}
