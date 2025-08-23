package net.shoreline.client.api.config;

import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.Setter;
import net.shoreline.client.Shoreline;
import net.shoreline.client.impl.module.client.ThemeModule;
import net.shoreline.client.impl.render.ColorUtil;

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

    public int getRed()
    {
        return getValue().getRed();
    }

    public int getGreen()
    {
        return getValue().getGreen();
    }

    public int getBlue()
    {
        return getValue().getBlue();
    }

    public int getAlpha()
    {
        return getValue().getAlpha();
    }

    public int getRGB()
    {
        return getValue().getRGB();
    }

    public float[] getHsb()
    {
        float[] hsbVals = Color.RGBtoHSB(getRed(), getGreen(), getBlue(), null);
        return new float[] { hsbVals[0], hsbVals[1], hsbVals[2], transparency ? getAlpha() / 255.0f : 1.0f };
    }

    public static class Builder extends ConfigBuilder<Color>
    {
        private boolean transparency;

        public Builder(String name) {
            super(name);
        }

        public Builder setRgb(int rgb)
        {
            setDefaultValue(new Color(rgb, (rgb & 0xff000000) != 0xff000000));
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
