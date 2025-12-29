package net.shoreline.client.api;

import lombok.RequiredArgsConstructor;
import net.minecraft.client.MinecraftClient;
import net.shoreline.eventbus.EventBus;

@RequiredArgsConstructor
public class GenericFeature implements Identifiable
{
    protected static final MinecraftClient mc = MinecraftClient.getInstance();

    private final String name;
    private final String[] nameAliases;

    public GenericFeature(String name)
    {
        this.name = name;
        this.nameAliases = new String[0];
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
        return String.format("%s_feature", name);
    }

    protected boolean checkNull()
    {
        return mc.player == null || mc.world == null;
    }
}
