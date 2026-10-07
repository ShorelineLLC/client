package net.shoreline.client.api.config;

import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.Setter;
import net.shoreline.client.impl.event.LoadingEvent;
import net.shoreline.client.impl.file.ModConfiguration;
import net.shoreline.client.impl.module.client.ThemeModule;
import net.shoreline.eventbus.EventBus;
import net.shoreline.eventbus.annotation.EventListener;

import java.awt.*;
import java.util.HexFormat;

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
        String value = Integer.toHexString(getRGB()) + "-" + global;
        jsonObject.addProperty("value", value);
        return jsonObject;
    }

    @EventListener
    public void onFinishedLoading(LoadingEvent.Finished event)
    {
        if (global)
        {
            setValue(ThemeModule.INSTANCE.getPrimaryColor());
        }
    }

    public void setGlobal(boolean global)
    {
        this.global = global;
        if (global)
        {
            ThemeModule.INSTANCE.addGlobal(this);
        }
        else
        {
            ThemeModule.INSTANCE.removeGlobal(this);
        }
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
        private boolean global;

        public Builder(String name) {
            super(name);
        }

        public Builder setGlobalColor()
        {
            setDefaultValue(Color.WHITE);
            global = true;
            return this;
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
            ColorConfig built = (ColorConfig) super.build();
            built.setTransparency(transparency);
            if (global)
            {
                built.setGlobal(true);
                EventBus.INSTANCE.subscribe(built);
            }

            return built;
        }
    }
}
