package net.shoreline.client.impl.render.animation;

import net.shoreline.client.impl.render.Easing;

import java.awt.*;

public class ColorAnimation extends Animation
{
    public ColorAnimation(float length)
    {
        super(length);
    }

    public ColorAnimation(boolean initial, float length)
    {
        super(initial, length, Easing.LINEAR);
    }

    public ColorAnimation(boolean initial, float length, Easing easing)
    {
        super(initial, length, easing);
    }

    public Color getColor(Color start, Color end)
    {
        double factor = getFactor();
        return new Color(
                (int) (start.getRed() + (end.getRed() - start.getRed()) * factor),
                (int) (start.getGreen() + (end.getGreen() - start.getGreen()) * factor),
                (int) (start.getBlue() + (end.getBlue() - start.getBlue()) * factor),
                (int) (start.getAlpha() + (end.getAlpha() - start.getAlpha()) * factor));
    }
}