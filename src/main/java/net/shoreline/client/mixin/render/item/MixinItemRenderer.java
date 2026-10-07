package net.shoreline.client.mixin.render.item;

import net.minecraft.client.item.ItemModelManager;
import net.minecraft.client.render.item.ItemRenderer;
import net.shoreline.client.impl.imixin.IItemRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ItemRenderer.class)
public abstract class MixinItemRenderer implements IItemRenderer
{
    @Override
    @Accessor(value = "itemModelManager")
    public abstract ItemModelManager getItemModelManager();
}
