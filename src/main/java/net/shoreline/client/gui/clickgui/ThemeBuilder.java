package net.shoreline.client.gui.clickgui;

public class ThemeBuilder
{
    private int titleColor;
    private int backgroundColor;
    private int outlineColor;
    private int moduleColor;
    private int settingColor;

    public ThemeBuilder setTitleColor(int color)
    {
        this.titleColor = color;
        return this;
    }

    public ThemeBuilder setBackgroundColor(int color)
    {
        this.backgroundColor = color;
        return this;
    }

    public ThemeBuilder setOutlineColor(int color)
    {
        this.outlineColor = color;
        return this;
    }

    public ThemeBuilder setModuleColor(int color)
    {
        this.moduleColor = color;
        return this;
    }

    public ThemeBuilder setSettingColor(int color)
    {
        this.settingColor = color;
        return this;
    }

    public Theme build()
    {
        return new Theme(titleColor, backgroundColor, outlineColor, moduleColor, settingColor);
    }
}
