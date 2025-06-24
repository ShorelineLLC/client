package net.shoreline.client.api.config;

import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.Setter;

import java.awt.*;

@Getter
@Setter
public class ColorConfig extends Config<Color>
{
    private boolean transparency;
    private boolean global;

    public ColorConfig(String name, String description)
    {
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

    public float[] getHsb()
    {
        float[] hsbVals = Color.RGBtoHSB(getValue().getRed(), getValue().getGreen(), getValue().getBlue(), null);
        return new float[]{ hsbVals[0], hsbVals[1], hsbVals[2], transparency ? getValue().getAlpha() / 255.0f : 1.0f };
    }

    public static class Builder extends ConfigBuilder<Color>
    {
        private boolean transparency;

        public Builder(String name) {
            super(name);
        }

        public Builder setRgb(int rgb)
        {
            setDefaultValue(new Color(rgb));
            return this;
        }

        public Builder setTransparency(boolean transparency)
        {
            this.transparency = transparency;
            return this;
        }

        @Override
        public Config<Color> build()
        {
            ColorConfig colorConfig = (ColorConfig) super.build();
            colorConfig.setTransparency(transparency);
            return colorConfig;
        }
    }
}
