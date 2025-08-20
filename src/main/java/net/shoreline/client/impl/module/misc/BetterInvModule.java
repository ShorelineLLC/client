package net.shoreline.client.impl.module.misc;

import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.api.module.Toggleable;

public class BetterInvModule extends Toggleable
{
    public BetterInvModule()
    {
        super("BetterInv", "Improves inventory interactions", GuiCategory.MISCELLANEOUS);
    }
}
