package net.shoreline.client.impl.module.world;

import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.api.module.Toggleable;

public class NukerModule extends Toggleable
{

    public NukerModule()
    {
        super("Nuker", "Clears nearby blocks", GuiCategory.WORLD);
    }
}
