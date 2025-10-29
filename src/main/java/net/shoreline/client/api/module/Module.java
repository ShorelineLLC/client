package net.shoreline.client.api.module;

import lombok.Getter;
import net.shoreline.client.api.config.ConfigContainer;

@Getter
public abstract class Module extends ConfigContainer
{
    public static final String ID_FORMAT = "%s_module";

    private final String description;
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
        return String.format(ID_FORMAT, getName().toLowerCase());
    }
}
