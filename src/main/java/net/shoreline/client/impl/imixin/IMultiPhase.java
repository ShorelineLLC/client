package net.shoreline.client.impl.imixin;

import net.minecraft.client.render.RenderLayer;

@IMixin
public interface IMultiPhase
{
    RenderLayer.MultiPhaseParameters hookGetPhases();
}
