package net.shoreline.client.impl.module.client;

import net.shoreline.client.api.config.ColorConfig;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.module.Concurrent;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.impl.render.Theme;

import java.awt.*;

public class ThemeModule extends Concurrent
{
    public static ThemeModule INSTANCE;

    Config<Color> primaryColor = new ColorConfig.Builder("PrimaryColor")
            .setRgb(0x5f5fde)
            .setDescription("The primary Clickgui color").build();
    Config<Color> titleColor = new ColorConfig.Builder("TitleColor")
            .setRgb(0x5f5fde)
            .setDescription("The Clickgui title color").build();
    Config<Color> backgroundColor = new ColorConfig.Builder("BackgroundColor")
            .setRgb(0x000000)
            .setDescription("The Clickgui background color").build();
    Config<Color> outlineColor = new ColorConfig.Builder("OutlineColor")
            .setRgb(0x000000)
            .setDescription("The Clickgui outline color").build();
    Config<Color> textColor = new ColorConfig.Builder("TextColor")
            .setRgb(0xffffff)
            .setDescription("The Clickgui text color").build();

    public ThemeModule()
    {
        super("Theme", "Customize client colors", GuiCategory.CLIENT);
        INSTANCE = this;

        Theme primaryTheme = ClickGuiModule.INSTANCE.getTheme();

        primaryColor.addListener(primaryTheme::setComponentColor);
        titleColor.addListener(primaryTheme::setTitleColor);
        backgroundColor.addListener(primaryTheme::setBackgroundColor);
        outlineColor.addListener(primaryTheme::setOutlineColor);
        textColor.addListener(primaryTheme::setTextColor);
    }

    public Color getPrimaryColor()
    {
        return primaryColor.getValue();
    }

    public Color getTitleColor()
    {
        return titleColor.getValue();
    }

    public Color getBackgroundColor()
    {
        return backgroundColor.getValue();
    }

    public Color getOutlineColor()
    {
        return outlineColor.getValue();
    }

    public Color getTextColor()
    {
        return textColor.getValue();
    }
}
