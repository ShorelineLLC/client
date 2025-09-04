package net.shoreline.client.impl.module.combat;

import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.impl.module.combat.trap.TrapModule;

public class AutoTrapModule extends TrapModule
{
    public AutoTrapModule()
    {
        super("AutoTrap", "Traps enemies with obsidian", GuiCategory.COMBAT);
    }
}
