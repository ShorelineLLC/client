package net.shoreline.client.impl.render;

import lombok.experimental.UtilityClass;

import java.awt.*;

@UtilityClass
public class ColorUtil
{
    public int interpolateColor(float value, int c1, int c2)
    {
        float[] s = getRGBValues(c1);
        float[] e = getRGBValues(c2);
        return new Color(s[0] * value + e[0] * (1.0f - value),
                s[1] * value + e[1] * (1.0f - value),
                s[2] * value + e[2] * (1.0f - value),
                s[3] * value + e[3] * (1.0f - value)).getRGB();
    }

    public int withTransparency(int color, float alpha)
    {
        if (alpha == 1.0f)
        {
            return color;
        }
        float colorAlpha = (color >> 24) & 0xff;
        alpha = Math.max(0.0f, Math.min(1.0f, alpha));
        int colorAlphaInt = Math.max(10, (int) (colorAlpha * alpha));
        return (colorAlphaInt << 24) | (color & 0xffffff);
    }

    public static int brighten(int color, int amount, float factor)
    {
        if (factor == 0.0f)
        {
            return color;
        }

        int a = (color >>> 24) & 0xff;
        int r = (color >>> 16) & 0xff;
        int g = (color >>> 8) & 0xff;
        int b = color & 0xff;

        a = Math.min(255, (int) (a + (amount * factor)));
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    public float[] getRGBValues(int color)
    {
        Color c = new Color(color);
        float r = c.getRed() / 255.0f;
        float g = c.getGreen() / 255.0f;
        float b = c.getBlue() / 255.0f;
        float a = c.getAlpha() / 255.0f;
        return new float[] { r, g, b, a };
    }
}
