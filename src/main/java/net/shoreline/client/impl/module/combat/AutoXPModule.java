package net.shoreline.client.impl.module.combat;

import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.api.module.Toggleable;

public class AutoXPModule extends Toggleable
{
    public AutoXPModule()
    {
        super("AutoXP", "Automatically mends armor", GuiCategory.COMBAT);
    }
}
