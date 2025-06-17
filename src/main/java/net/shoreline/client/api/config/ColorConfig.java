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
        JsonObject jsonObject = super.toJson();
        jsonObject.addProperty("value", Integer.toHexString(getRGB()));
        return jsonObject;
    }

    public int getRGB()
    {
        return getValue().getRGB();
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
