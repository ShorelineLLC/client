package net.shoreline.client.impl.event.buffers;

import net.shoreline.client.init.Modules;

import java.awt.*;

public class ColorBuffer {

    public static Color getClientColor()
    {
        return Modules.COLORS.getColor();
    }

    public static int getClientRgb()
    {
        return Modules.COLORS.getRGB();
    }
}
