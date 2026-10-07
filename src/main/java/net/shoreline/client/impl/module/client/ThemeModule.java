package net.shoreline.client.impl.module.client;

import net.shoreline.client.api.config.ColorConfig;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.module.Concurrent;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.api.module.Module;
import net.shoreline.client.impl.Managers;
import net.shoreline.client.impl.render.ColorUtil;
import net.shoreline.client.impl.render.Theme;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ThemeModule extends Concurrent
{
    public static ThemeModule INSTANCE;
    public static final int COLOR = -2;

    Config<Color> primaryColor = new ColorConfig.Builder("PrimaryColor")
            .setRgb(0xff5f5fde)
            .setDescription("The primary Clickgui color").build();
    Config<Color> titleColor = new ColorConfig.Builder("TitleColor")
            .setRgb(0xff5f5fde)
            .setDescription("The Clickgui title color").build();
    Config<Color> backgroundColor = new ColorConfig.Builder("BackgroundColor")
            .setRgb(0xff000000)
            .setDescription("The Clickgui background color").build();
    Config<Color> outlineColor = new ColorConfig.Builder("OutlineColor")
            .setRgb(0x00000000).setTransparency(true)
            .setDescription("The Clickgui outline color").build();
    Config<Color> textColor = new ColorConfig.Builder("TextColor")
            .setRgb(0xffffffff)
            .setDescription("The Clickgui text color").build();

    private final List<ColorConfig> globals = new ArrayList<>();

    public ThemeModule()
    {
        super("Theme", "Customize client colors", GuiCategory.CLIENT);
        INSTANCE = this;

        Theme primaryTheme = ClickGuiModule.INSTANCE.getTheme();
        primaryColor.addObserver(value ->
        {
            primaryTheme.setComponentColor(value);
            for (ColorConfig config : globals)
            {
                updateGlobalColor(value, config);
            }
        });

        titleColor.addObserver(primaryTheme::setTitleColor);
        backgroundColor.addObserver(primaryTheme::setBackgroundColor);
        outlineColor.addObserver(primaryTheme::setOutlineColor);
        textColor.addObserver(primaryTheme::setTextColor);
    }

    public Config<Color> getPrimaryConfig()
    {
        return primaryColor;
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

    public Config<Color> getSetting()
    {
        return primaryColor;
    }

    public void updateGlobalColor(Color value, ColorConfig global)
    {
        global.setValue(new Color(ColorUtil.withTransparency(value, global.getAlpha() / 255f), true));
    }

    public void addGlobal(ColorConfig config)
    {
        this.updateGlobalColor(primaryColor.getValue(), config);
        this.globals.add(config);
    }

    public void removeGlobal(ColorConfig config)
    {
        this.globals.remove(config);
    }
}
