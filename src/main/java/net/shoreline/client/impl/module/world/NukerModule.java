package net.shoreline.client.impl.module.world;

import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.module.ToggleModule;

public class NukerModule extends ToggleModule
{
    public NukerModule()
    {
        super("Nuker", "Clears nearby blocks", ModuleCategory.WORLD);
    }
}
