package net.shoreline.client.api.config;

import com.google.gson.JsonObject;

public class BooleanConfig extends Config<Boolean>
{
    public BooleanConfig(String name, String description) {
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

    public static class Builder extends ConfigBuilder<Boolean>
    {
        public Builder(String name, String description) {
            super(name, description);
        }
    }
}
