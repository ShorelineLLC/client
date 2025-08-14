package net.shoreline.client.mixin.render;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.item.ItemRenderState;
import net.minecraft.client.render.model.BakedModel;
import net.shoreline.client.impl.imixin.ILayerRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ItemRenderState.LayerRenderState.class)
public abstract class MixinLayerRenderState implements ILayerRenderState
{
    @Override
    @Accessor(value = "renderLayer")
    public abstract void setRenderLayer(RenderLayer layer);
}
