package net.shoreline.client.api.config;

import java.awt.*;

public class ColorConfig extends Config<Color> {
    public ColorConfig(String name, String description) {
        super(name, description);
    }

    public static class Builder extends ConfigBuilder<Color>
    {
        public Builder(String name, String description) {
            super(name, description);
        }

        public void setRgb(int rgb)
        {
            setDefaultValue(new Color(rgb));
        }
    }
}
