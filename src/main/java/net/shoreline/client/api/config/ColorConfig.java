package net.shoreline.client.api.config;

import com.google.gson.JsonObject;

import java.awt.*;

public class ColorConfig extends Config<Color>
{
    public ColorConfig(String name, String description) {
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

    public static class Builder extends ConfigBuilder<Color>
    {
        public Builder(String name) {
            super(name);
        }

        public Builder setRgb(int rgb)
        {
            setDefaultValue(new Color(rgb));
            return this;
        }
    }
}
