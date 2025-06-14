package net.shoreline.client.api.module;

import com.google.gson.JsonObject;
import net.shoreline.eventbus.EventBus;

public class Concurrent extends Module
{
    public Concurrent(final String name,
                      final String description,
                      final GuiCategory category)
    {
        super(name, description, category);
        EventBus.INSTANCE.subscribe(this);
    }

    public Concurrent(final String name,
                      final String[] nameAliases,
                      final String description,
                      final GuiCategory category)
    {
        super(name, nameAliases, description, category);
        EventBus.INSTANCE.subscribe(this);
    }

    @Override
    public JsonObject toJson()
    {
        return null;
    }

    @Override
    public void fromJson(JsonObject jsonObject)
    {

    }
}
