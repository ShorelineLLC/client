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

    public int withTransparency(Color color, float alpha)
    {
        float[] rgb = getRGBValues(color.getRGB());
        return new Color(rgb[0], rgb[1], rgb[2], alpha).getRGB();
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
        Color c = new Color(color, (color >>> 24) != 0);
        float r = c.getRed() / 255.0f;
        float g = c.getGreen() / 255.0f;
        float b = c.getBlue() / 255.0f;
        float a = c.getAlpha() / 255.0f;
        return new float[] { r, g, b, a };
    }

    public int[] getRGBColorValues(int color)
    {
        int r = (color >> 16) & 0xff;
        int g = (color >> 8) & 0xff;
        int b = (color) & 0xff;
        int a = (color & 0xff000000) != 0xff000000 ? 255 : (color >> 24) & 0xff;
        return new int[] { r, g, b, a };
    }

    public static Color hslToColor(float f, float f2, float f3, float f4)
    {
        f %= 360.0f;
        float f5;
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

    public static float getRainbowHue(float speedFactor, int offset)
    {
        float speed = 2500.0f / speedFactor;
        return ((System.currentTimeMillis() + offset) % (int) speed) / speed;
    }

    public static float getPulse(float speedFactor, float depthFactor, float value, int offset)
    {
        float time = (float) Math.sin(getRainbowHue(speedFactor / 36, (int) (offset / speedFactor)) * 360);
        float variation = time * (depthFactor / 8);
        return Math.max(0f, Math.min(1f, value - (depthFactor / 8) + variation));
    }

    public static int getPulse(float speedFactor, float depthFactor, int offset, int color)
    {
        int[] rgba = getRGBColorValues(color);
        float[] hsb = Color.RGBtoHSB(rgba[0], rgba[1], rgba[2], null);
        return Color.HSBtoRGB(hsb[0], hsb[1], getPulse(speedFactor, depthFactor, hsb[2], -offset));
    }
}
