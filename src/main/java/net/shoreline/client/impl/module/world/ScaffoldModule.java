package net.shoreline.client.impl.module.world;

import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.impl.module.impl.PlacerModule;

public class ScaffoldModule extends PlacerModule
{

    public ScaffoldModule()
    {
        super("Scaffold", new String[] {"BlockFly"}, "Places blocks under the player", GuiCategory.WORLD);
    }
}
