package net.shoreline.client.impl.module.client;

import net.shoreline.client.api.module.Concurrent;
import net.shoreline.client.api.module.GuiCategory;

public class BaritoneModule extends Concurrent
{
    public BaritoneModule()
    {
        super("Baritone", "Manage Baritone settings", GuiCategory.CLIENT);
    }
}
