package net.shoreline.client.impl.event.color.world;

import net.minecraft.world.biome.ColorResolver;
import net.shoreline.eventbus.annotation.Cancelable;
import net.shoreline.eventbus.event.Event;

import java.awt.*;

@Cancelable
public class BiomeColorEvent extends Event
{

    private final ColorResolver colorResolver;
    private Color color;

    public BiomeColorEvent(ColorResolver colorResolver)
    {
        this.colorResolver = colorResolver;
    }

    public ColorResolver getColorResolver()
    {
        return colorResolver;
    }

    public void setColor(Color color)
    {
        this.color = color;
    }

    public Color getColor()
    {
        return color;
    }

    public int getRGB()
    {
        return color.getRGB();
    }
}
