package net.shoreline.client.api.module;

import lombok.Getter;
import net.minecraft.client.MinecraftClient;
import net.shoreline.client.api.Identifiable;
import net.shoreline.client.api.config.Configuration;

public class Module extends Configuration implements Identifiable
{
    protected static final MinecraftClient mc = MinecraftClient.getInstance();

    private final String name;
    private final String[] nameAliases;

    @Getter
    private final String description;

    @Getter
    private final GuiCategory category;

    public Module(final String name,
                  final String description,
                  final GuiCategory category)
    {
        this.name = name;
        this.nameAliases = new String[0];
        this.description = description;
        this.category = category;
    }

    public Module(final String name,
                  final String[] nameAliases,
                  final String description,
                  final GuiCategory category)
    {
        this.name = name;
        this.nameAliases = nameAliases;
        this.description = description;
        this.category = category;
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public String[] getAliases()
    {
        return nameAliases;
    }

    @Override
    public String getId()
    {
        return String.format("%s_module", name.toLowerCase());
    }
}
