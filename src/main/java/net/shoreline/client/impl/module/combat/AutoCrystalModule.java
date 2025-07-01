package net.shoreline.client.impl.module.combat;

import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.api.module.Toggleable;

public class AutoCrystalModule extends Toggleable
{
    public static AutoCrystalModule INSTANCE;

    public AutoCrystalModule()
    {
        super("AutoCrystal", new String[] {"CrystalAura"}, "Best CA on the market", GuiCategory.COMBAT);
        INSTANCE = this;
    }


}
