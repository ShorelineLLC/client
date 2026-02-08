package net.shoreline.client.impl.module.combat.trap;

import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.impl.module.impl.ObsidianPlacerModule;

import java.util.EnumSet;

public abstract class TrapModule extends ObsidianPlacerModule
{
    protected final TrapPositionCalc trapPos = new TrapPositionCalc();

    public TrapModule(String name, String description, GuiCategory category)
    {
        super(name, description, category);
    }

    public TrapModule(String name, String[] nameAliases, String description, GuiCategory category)
    {
        super(name, nameAliases, description, category);
    }

    public abstract EnumSet<TrapLayer> getLayers();
}
