package net.shoreline.client.impl.module.misc;

import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.api.module.Toggleable;

public class ItemPreviewModule extends Toggleable
{

    public ItemPreviewModule()
    {
        super("ItemPreview", "Shows preview of items in inventory", GuiCategory.MISCELLANEOUS);
    }
}
