package net.shoreline.client.impl.imixin;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderPhase;

@IMixin
public interface IMultiPhaseParameters
{
    RenderLayer.OutlineMode getOutlineMode();

    RenderPhase.TextureBase getTexture();
}
