package net.shoreline.client.impl.module.misc;

import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.api.module.Toggleable;

public class ChestSwapModule extends Toggleable
{
    public ChestSwapModule()
    {
        super("ChestSwap", "Swaps elytra and chestplate", GuiCategory.MISCELLANEOUS);
    }
}
