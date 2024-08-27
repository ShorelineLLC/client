package net.shoreline.client.api.config.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.impl.module.client.ColorsModule;

import java.awt.*;
import java.util.function.Supplier;

/**
 * @author linus
 * @since 1.0
 */
public class ColorConfig extends Config<Color>
{
    // RGB value of the current Color value
    private final boolean allowAlpha;
    //
    private boolean global;

    public ColorConfig(String name, String desc, Color value, boolean allowAlpha, boolean global)
    {
        super(name, desc, value);
        this.allowAlpha = allowAlpha;
        setGlobal(global);
    }

    public ColorConfig(String name, String desc, Color value)
    {
        this(name, desc, value, true, true);
    }

    public ColorConfig(String name, String desc, Color value, boolean allowAlpha, boolean global, Supplier<Boolean> visible)
    {
        super(name, desc, value, visible);
        this.allowAlpha = allowAlpha;
        setGlobal(global);
    }

    public ColorConfig(String name, String desc, Color value, boolean allowAlpha, Supplier<Boolean> visible)
    {
        this(name, desc, value, allowAlpha, true, visible);
    }

    public ColorConfig(String name, String desc, Color value, Supplier<Boolean> visible)
    {
        this(name, desc, value, true, visible);
    }

    public ColorConfig(String name, String desc, Integer rgb, boolean allowAlpha)
    {
        this(name, desc, new Color(rgb, (rgb & 0xff000000) != 0xff000000), allowAlpha, true);
    }

    public ColorConfig(String name, String desc, Integer value)
    {
        this(name, desc, value, false);
    }

    @Override
    public Color getValue()
    {
        if (ColorsModule.getInstance() != null && global)
        {
            return ColorsModule.getInstance().getColor(getAlpha());
        }
        return new Color(value.getRed(), value.getGreen(), value.getBlue(), allowAlpha ? value.getAlpha() : 255);
    }

    public Color getValue(int alpha)
    {
        if (ColorsModule.getInstance() != null && global)
        {
            return ColorsModule.getInstance().getColor(alpha);
        }
        return new Color(value.getRed(), value.getGreen(), value.getBlue(), alpha);
    }

    /**
     * @param val
     */
    public void setValue(int val)
    {
        Color color = new Color(val, (val & 0xff000000) != 0xff000000);
        setValue(color);
    }

    public int getRgb()
    {
        return getValue().getRGB();
    }

    public int getRgb(int alpha)
    {
        return getValue(alpha).getRGB();
    }

    public int getRed()
    {
        return value.getRed();
    }

    public int getGreen()
    {
        return value.getGreen();
    }

    public int getBlue()
    {
        return value.getBlue();
    }

    public boolean allowAlpha()
    {
        return allowAlpha;
    }

    public int getAlpha()
    {
        return allowAlpha ? value.getAlpha() : 255;
    }

    public float[] getHsb()
    {
        float[] hsbVals = Color.RGBtoHSB(value.getRed(), value.getGreen(), value.getBlue(), null);
        return new float[]{hsbVals[0], hsbVals[1], hsbVals[2], allowAlpha ? value.getAlpha() / 255.0f : 1.0f};
    }

    public boolean isGlobal()
    {
        return global;
    }

    public void setGlobal()
    {
        setGlobal(true);
    }

    public void setGlobal(boolean global)
    {
        this.global = global;
        configAnimation.setState(global);
        if (ColorsModule.getInstance() != null && global)
        {
            setValue(ColorsModule.getInstance().getColor(getAlpha()));
        }
    }

    /**
     * @return
     */
    @Override
    public JsonObject toJson()
    {
        JsonObject configObj = super.toJson();
        // hex value for readability
        configObj.addProperty("value", Integer.toHexString(getRgb()));
        configObj.addProperty("global", global);
        return configObj;
    }

    /**
     * @param jsonObj The data as a json object
     * @return
     */
    @Override
    public Color fromJson(JsonObject jsonObj)
    {
        if (jsonObj.has("value"))
        {
            JsonElement element = jsonObj.get("value");
            String hex = element.getAsString();
            if (jsonObj.has("global"))
            {
                JsonElement element1 = jsonObj.get("global");
                setGlobal(element1.getAsBoolean());
            }
            Color color = parseColor(hex);
            return color;
        }
        return null;
    }

    /**
     * @param colorString
     * @return
     * @throws IllegalArgumentException
     */
    public Color parseColor(String colorString)
    {
        return new Color((int) Long.parseLong(colorString, 16), true);
    }
}
