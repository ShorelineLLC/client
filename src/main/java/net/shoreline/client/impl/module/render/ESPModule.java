package net.shoreline.client.impl.module.render;

import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.api.module.Toggleable;

public class ESPModule extends Toggleable
{

    public ESPModule()
    {
        super("ESP", "Highlights entities", GuiCategory.RENDER);
    }
}
