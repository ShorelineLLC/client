package net.shoreline.client.mixin.render;

import net.minecraft.client.render.RenderLayer;
import net.shoreline.client.impl.imixin.IMultiPhase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(RenderLayer.MultiPhase.class)
public abstract class MixinMultiPhase implements IMultiPhase
{
    @Override
    @Invoker("getPhases")
    public abstract RenderLayer.MultiPhaseParameters hookGetPhases();
}
