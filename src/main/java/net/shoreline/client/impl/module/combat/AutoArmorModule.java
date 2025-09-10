package net.shoreline.client.impl.module.combat;

import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.api.module.Toggleable;

public class AutoArmorModule extends Toggleable
{
    public AutoArmorModule()
    {
        super("AutoArmor", "Automatically equips armor", GuiCategory.COMBAT);
    }
}
