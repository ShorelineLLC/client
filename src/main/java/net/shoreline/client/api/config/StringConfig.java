package net.shoreline.client.api.config;

import com.google.gson.JsonObject;

public class StringConfig extends Config<String>
{
    public StringConfig(String name, String description) {
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

    public static class Builder extends ConfigBuilder<String>
    {
        public Builder(String name, String description) {
            super(name, description);
        }
    }
}
