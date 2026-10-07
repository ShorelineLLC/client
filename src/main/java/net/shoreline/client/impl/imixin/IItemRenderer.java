package net.shoreline.client.impl.imixin;

import net.minecraft.client.item.ItemModelManager;

@IMixin
public interface IItemRenderer
{
    ItemModelManager getItemModelManager();
}
