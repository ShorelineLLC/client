package net.shoreline.client.impl.render;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.shoreline.client.impl.render.animation.Animation;

import java.awt.*;

@RequiredArgsConstructor
public class Theme
{
    private final Animation fadeAnimation;

    private int titleColor;
    private int componentColor;
    private int textColor;
    private int backgroundColor;
    private int outlineColor;

    public void setTitleColor(Color color)
    {
        this.titleColor = ColorUtil.withTransparency(color, 0.4f);
    }

    public void setComponentColor(Color color)
    {
        this.componentColor = ColorUtil.withTransparency(color, 0.4f);
    }

    public void setTextColor(Color color)
    {
        this.textColor = color.getRGB();
    }

    public void setBackgroundColor(Color color)
    {
        this.backgroundColor = ColorUtil.withTransparency(color, 0.33f);
    }

    public void setOutlineColor(Color color)
    {
        this.outlineColor = color.getRGB();
    }

    public int getTitleColor()
    {
        return getColor(titleColor, 1.0f);
    }

    public int getOutlineColor()
    {
        return getColor(outlineColor, 1.0f);
    }

    public int getComponentColor()
    {
        return getComponentColor(1.0f);
    }

    public int getComponentColor(float transparency)
    {
        return getColor(componentColor, transparency);
    }

    public int getTextColor()
    {
        return getTextColor(1.0f);
    }

    public int getTextColor(float transparency)
    {
        return getColor(textColor, transparency);
    }

    public int getBackgroundColor()
    {
        return getColor(backgroundColor, 1.0f);
    }

    public int getColor(int color, float transparency)
    {
        return ColorUtil.withTransparency(color, (float) fadeAnimation.getFactor() * transparency);
    }
}