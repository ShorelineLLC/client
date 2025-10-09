package net.shoreline.client.impl.module.world;

import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.api.module.Toggleable;

public class XRayModule extends Toggleable
{

    public XRayModule()
    {
        super("XRay", "See through blocks", GuiCategory.WORLD);
    }
}
