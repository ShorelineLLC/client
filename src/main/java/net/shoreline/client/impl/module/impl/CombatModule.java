package net.shoreline.client.impl.module.impl;

import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.api.module.Toggleable;

public class CombatModule extends Toggleable
{

    public CombatModule(String name, String description, GuiCategory category) {
        super(name, description, category);
    }

    public CombatModule(final String name,
                        final String[] nameAliases,
                        final String description,
                        final GuiCategory category)
    {
        super(name, nameAliases, description, category);
    }
}
