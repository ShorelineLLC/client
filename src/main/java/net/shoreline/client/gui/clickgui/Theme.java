package net.shoreline.client.gui.clickgui;

import lombok.Builder;
import net.shoreline.client.impl.render.Animation;
import net.shoreline.client.impl.render.ColorUtil;

@Builder
public class Theme
{
    private final Animation fadeAnimation;

    private final int titleColor;
    private final int componentColor;
    private final int textColor;
    private final int backgroundColor;
    private final int outlineColor;

    public int getTitleColor()
    {
        return getColor(titleColor, 1.0f);
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

    public int getOutlineColor()
    {
        return getColor(outlineColor, 1.0f);
    }

    public int getColor(int color, float transparency)
    {
        return ColorUtil.withTransparency(color, (float) fadeAnimation.getFactor() * transparency);
    }
}
