package net.shoreline.client.impl.imixin;

import net.minecraft.client.render.RenderLayer;

@IMixin
public interface ILayerRenderState
{
    void setRenderLayer(RenderLayer layer);
}
