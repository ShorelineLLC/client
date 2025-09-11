package net.shoreline.client.impl.module.misc;

import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.api.module.Toggleable;

public class NameProtectModule extends Toggleable
{
    public NameProtectModule()
    {
        super("NameProtect", "Censors your name", GuiCategory.MISCELLANEOUS);
    }
}
