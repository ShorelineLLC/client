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
        JsonObject jsonObject = super.toJson();
        jsonObject.addProperty("value", getValue());
        return jsonObject;
    }

    public static class Builder extends ConfigBuilder<Boolean>
    {
        public Builder(String name) {
            super(name);
        }
    }
}
