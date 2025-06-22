package net.shoreline.client.impl.module.client;

import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.api.module.Toggleable;

public class FontModule extends Toggleable
{
    public static FontModule INSTANCE;

    public FontModule()
    {
        super("Font", "Client custom fonts", GuiCategory.CLIENT);
        INSTANCE = this;
    }
}
