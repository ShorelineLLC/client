package net.shoreline.client.impl.imixin;

import net.minecraft.client.render.item.ItemRenderState;

@IMixin
public interface IItemRenderState
{
    int getLayerCount();

    ItemRenderState.LayerRenderState[] getLayers();
}
