package net.shoreline.client.api.config;

import com.google.gson.JsonObject;

import java.util.List;

public class ConfigGroup extends Config<List<Config<?>>>
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

    @Override
    public void fromJson(JsonObject jsonObject)
    {

    }
}
