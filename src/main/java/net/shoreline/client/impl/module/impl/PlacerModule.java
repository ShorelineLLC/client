package net.shoreline.client.impl.module.impl;

import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.api.module.Toggleable;
import net.shoreline.client.impl.module.client.AnticheatModule;

public class PlacerModule extends Toggleable
{
    protected final AnticheatModule anticheat = AnticheatModule.INSTANCE;

    public PlacerModule(String name, String description, GuiCategory category) {
        super(name, description, category);
    }

    public PlacerModule(final String name,
                        final String[] nameAliases,
                        final String description,
                        final GuiCategory category)
    {
        super(name, nameAliases, description, category);
    }
}
