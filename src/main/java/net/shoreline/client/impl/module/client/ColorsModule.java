package net.shoreline.client.impl.module.client;

import net.shoreline.client.api.config.ColorConfig;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.module.Concurrent;
import net.shoreline.client.api.module.GuiCategory;

import java.awt.*;

public class ColorsModule extends Concurrent
{
    Config<Color> primaryColor = new ColorConfig.Builder("PrimaryColor")
            .setRgb(0x663500a4)
            .setDescription("The primary Clickgui color").build();
    Config<Color> titleColor = new ColorConfig.Builder("TitleColor")
            .setRgb(0x663500a4)
            .setDescription("The Clickgui title color").build();
    Config<Color> backgroundColor = new ColorConfig.Builder("BackgroundColor")
            .setRgb(0x332e0094)
            .setDescription("The Clickgui background color").build();
    Config<Color> outlineColor = new ColorConfig.Builder("OutlineColor")
            .setRgb(0x1a7a3cf2)
            .setDescription("The Clickgui outline color").build();
    Config<Color> textColor = new ColorConfig.Builder("TextColor")
            .setRgb(0xffffffff)
            .setDescription("The Clickgui text color").build();

    public ColorsModule()
    {
        super("Colors", "Customize client colors", GuiCategory.CLIENT);
    }
}
