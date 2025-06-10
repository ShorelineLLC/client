package net.shoreline.client.api.module;

public class Concurrent extends Module
{
    public Concurrent(final String name,
                      final String description,
                      final GuiCategory category)
    {
        super(name, description, category);
    }

    public Concurrent(final String name,
                      final String[] nameAliases,
                      final String description,
                      final GuiCategory category)
    {
        super(name, nameAliases, description, category);
    }
}
