package net.shoreline.client.impl.command.util.impl;

import net.shoreline.client.api.config.ColorConfig;
import net.shoreline.client.impl.command.util.IConfigParser;

import java.awt.*;

public class ColorConfigParser implements IConfigParser<Color, ColorConfig>
{
    @Override
    public boolean parseString(ColorConfig config, String string)
    {
        String hex = Integer.toHexString(config.getRGB());
        config.setValue(new Color((int) Long.parseLong(hex, 16), true));
        return true;
    }
}