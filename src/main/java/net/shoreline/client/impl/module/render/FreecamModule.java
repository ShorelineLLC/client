package net.shoreline.client.impl.module.render;

import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.api.module.Toggleable;

public class FreecamModule extends Toggleable
{

    public FreecamModule()
    {
        super("Freecam", "Look around freely", GuiCategory.RENDER);
    }
}
