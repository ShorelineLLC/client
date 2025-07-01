package net.shoreline.client.impl.module.combat;

import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.impl.module.impl.ObsidianPlacerModule;

public class SelfTrapModule extends ObsidianPlacerModule
{
    public SelfTrapModule()
    {
        super("SelfTrap", "Traps the player", GuiCategory.COMBAT);
    }
}
