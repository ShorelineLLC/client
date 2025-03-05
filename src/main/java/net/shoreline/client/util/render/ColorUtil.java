package net.shoreline.client.util.render;


import java.awt.*;

public class ColorUtil
{
    public static Color hslToColor(float f, float f2, float f3, float f4)
    {
        if (f2 < 0.0f || f2 > 100.0f)
        {
            throw new IllegalArgumentException("Color parameter outside of expected range - Saturation");
        }
        if (f3 < 0.0f || f3 > 100.0f)
        {
            throw new IllegalArgumentException("Color parameter outside of expected range - Lightness");
        }
        if (f4 < 0.0f || f4 > 1.0f)
        {
            throw new IllegalArgumentException("Color parameter outside of expected range - Alpha");
        }
        f %= 360.0f;
        float f5 = 0.0f;
        f5 = (double) f3 < 0.5 ? f3 * (1.0f + f2) : (f3 /= 100.0f) + (f2 /= 100.0f) - f2 * f3;
        f2 = 2.0f * f3 - f5;
        f3 = Math.max(0.0f, colorCalc(f2, f5, (f /= 360.0f) + 0.33333334f));
        float f6 = Math.max(0.0f, colorCalc(f2, f5, f));
        f2 = Math.max(0.0f, colorCalc(f2, f5, f - 0.33333334f));
        f3 = Math.min(f3, 1.0f);
        f6 = Math.min(f6, 1.0f);
        f2 = Math.min(f2, 1.0f);
        return new Color(f3, f6, f2, f4);
    }

    public static Color interpolateColor(float value, Color start, Color end)
    {
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
                sa * value + ea * (1.0f - value));
    }

    private static float colorCalc(float f, float f2, float f3)
    {
        if (f3 < 0.0f)
        {
            f3 += 1.0f;
        }
        if (f3 > 1.0f)
        {
            f3 -= 1.0f;
        }
        if (6.0f * f3 < 1.0f)
        {
            float f4 = f;
            return f4 + (f2 - f4) * 6.0f * f3;
        }
        if (2.0f * f3 < 1.0f)
        {
            return f2;
        }
        if (3.0f * f3 < 2.0f)
        {
            float f5 = f;
            return f5 + (f2 - f5) * 6.0f * (0.6666667f - f3);
        }
        return f;
    }

    public static int withAlpha(int color, float opacity)
    {
        return withAlpha(color, (int) (255 * opacity));
    }

    public static int withAlpha(int color, int alpha)
    {
        int red = 0xFF & (color >> 16);
        int blue = 0xFF & color;
        int green = 0xFF & (color >> 8);
        return (alpha << 24) | (red << 16) | (green << 8) | blue;
    }

    public static int fixTransparency(int color, float alpha)
    {
        if (alpha == 1.0F)
        {
            return color;
        }
        float colorAlpha = (color >> 24) & 0xFF;
        alpha = Math.max(0.0F, Math.min(1.0F, alpha));
        int colorAlphaInt = Math.max(10, (int) (colorAlpha * alpha));
        return (colorAlphaInt << 24) | (color & 0xFFFFFF);
    }
}
