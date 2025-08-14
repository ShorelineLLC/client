package net.shoreline.client.mixin.render.item;

import net.minecraft.client.render.item.ItemRenderState;
import net.shoreline.client.impl.imixin.IItemRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ItemRenderState.class)
public abstract class MixinItemRenderState implements IItemRenderState
{
    @Override
    @Accessor(value = "layerCount")
    public abstract int getLayerCount();

    @Override
    @Accessor(value = "layers")
    public abstract ItemRenderState.LayerRenderState[] getLayers();
}
