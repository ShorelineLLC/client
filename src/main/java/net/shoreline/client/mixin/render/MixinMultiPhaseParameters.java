package net.shoreline.client.mixin.render;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderPhase;
import net.shoreline.client.impl.imixin.IMultiPhaseParameters;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(RenderLayer.MultiPhaseParameters.class)
public abstract class MixinMultiPhaseParameters implements IMultiPhaseParameters
{
    @Override
    @Accessor("outlineMode")
    public abstract RenderLayer.OutlineMode getOutlineMode();

    @Override
    @Accessor("texture")
    public abstract RenderPhase.TextureBase getTexture();

    @Override
    @Accessor("target")
    public abstract RenderPhase.Target getTarget();
}
