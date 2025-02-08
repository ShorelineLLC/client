package net.shoreline.client.mixin.accessor;

import net.minecraft.client.render.RenderLayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(RenderLayer.MultiPhase.class)
public interface AccessorMultiPhase
{
    @Invoker("getPhases")
    RenderLayer.MultiPhaseParameters hookGetPhases();
}
