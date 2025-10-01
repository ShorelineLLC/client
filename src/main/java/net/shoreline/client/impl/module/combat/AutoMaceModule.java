package net.shoreline.client.impl.module.combat;

import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.api.module.Toggleable;

public class AutoMaceModule extends Toggleable
{

    public AutoMaceModule()
    {
        super("AutoMace", "Automatically damages entities with a mace", GuiCategory.COMBAT);
    }


}
