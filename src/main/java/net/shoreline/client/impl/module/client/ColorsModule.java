package net.shoreline.client.impl.module.client;

import net.shoreline.client.api.module.Concurrent;
import net.shoreline.client.api.module.GuiCategory;

public class ColorsModule extends Concurrent
{
    public ColorsModule()
    {
        super("Colors", "Customize client colors", GuiCategory.CLIENT);
    }
}
