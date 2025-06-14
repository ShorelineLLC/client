package net.shoreline.client.api.module;

import lombok.Getter;
import net.shoreline.client.api.config.Configuration;

public abstract class Module extends Configuration
{
    @Getter
    private final String description;

    @Getter
    private final GuiCategory category;

    public Module(final String name,
                  final String description,
                  final GuiCategory category)
    {
        super(name, new String[0]);
        this.description = description;
        this.category = category;
    }

    public Module(final String name,
                  final String[] nameAliases,
                  final String description,
                  final GuiCategory category)
    {
        super(name, nameAliases);
        this.description = description;
        this.category = category;
    }

    @Override
    public String getId()
    {
        return String.format("%s_module", getName().toLowerCase());
    }
}
