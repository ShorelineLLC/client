package net.shoreline.client.impl.event.buffers;

import net.shoreline.client.impl.module.client.ColorsModule;

import java.awt.*;

public class ColorBuffer {

    public static Color getClientColor()
    {
        return ColorsModule.INSTANCE.getColor();
    }

    public static int getClientRgb()
    {
        return ColorsModule.INSTANCE.getRGB();
    }
}
