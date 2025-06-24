package net.shoreline.client.api.config;

import com.google.gson.JsonObject;

public class ConfigGroup<T> extends Config<T>
{
    public ConfigGroup(String name, String description)
    {
        super(name, description);
    }

    @Override
    public JsonObject toJson()
    {
        return null;
    }
}
