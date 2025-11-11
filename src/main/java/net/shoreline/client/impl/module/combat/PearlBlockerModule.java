package net.shoreline.client.impl.module.combat;

import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.api.module.Toggleable;

public class PearlBlockerModule extends Toggleable
{

    public PearlBlockerModule()
    {
        super("PearlBlocker", "Blocks thrown ender pearls", GuiCategory.COMBAT);
    }

}
