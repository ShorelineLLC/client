package net.shoreline.client.impl.render;

import lombok.experimental.UtilityClass;

import java.awt.*;

@UtilityClass
public class ColorUtil
{
    public int interpolateColor(float value, int c1, int c2)
    {
        Color start = new Color(c1);
        Color end = new Color(c2);
        float sr = start.getRed() / 255.0f;
        float sg = start.getGreen() / 255.0f;
        float sb = start.getBlue() / 255.0f;
        float sa = start.getAlpha() / 255.0f;
        float er = end.getRed() / 255.0f;
        float eg = end.getGreen() / 255.0f;
        float eb = end.getBlue() / 255.0f;
        float ea = end.getAlpha() / 255.0f;
        return new Color(sr * value + er * (1.0f - value),
                sg * value + eg * (1.0f - value),
                sb * value + eb * (1.0f - value),
                sa * value + ea * (1.0f - value)).getRGB();
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
}
