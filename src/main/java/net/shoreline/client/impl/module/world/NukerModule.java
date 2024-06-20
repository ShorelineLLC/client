package net.shoreline.client.impl.module.world;

import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.setting.BooleanConfig;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.module.ToggleModule;

public class NukerModule extends ToggleModule
{
    Config<Boolean> flattenConfig = register(new BooleanConfig("Flatten", "Only clears above the player y-level", false));
    Config<Boolean> rotateConfig = register(new BooleanConfig("Rotate", "Rotates before mining blocks", false));

    public NukerModule()
    {
        super("Nuker", "Clears nearby blocks", ModuleCategory.WORLD);
    }
}
