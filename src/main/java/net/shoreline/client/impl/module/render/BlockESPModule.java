package net.shoreline.client.impl.module.render;

import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.api.module.Toggleable;

public class BlockESPModule extends Toggleable
{
    public BlockESPModule()
    {
        super("BlockESP", "Highlight any block in the world", GuiCategory.RENDER);
    }
}
