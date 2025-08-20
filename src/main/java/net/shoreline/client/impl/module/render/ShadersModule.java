package net.shoreline.client.impl.module.render;

import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.api.module.Toggleable;

public class ShadersModule extends Toggleable
{

    public ShadersModule()
    {
        super("Shaders", "Renders shaders over entities", GuiCategory.RENDER);
    }


}
