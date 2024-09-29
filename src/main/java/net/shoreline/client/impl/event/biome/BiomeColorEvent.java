package net.shoreline.client.impl.event.biome;

import net.shoreline.eventbus.annotation.Cancelable;
import net.shoreline.eventbus.event.Event;

import java.awt.*;

public class BiomeColorEvent extends Event
{
    private int color;

    public int getColor()
    {
        return color;
    }

    public void setColor(Color color)
    {
        this.color = color.getRGB();
    }

    public void setColor(int color)
    {
        this.color = color;
    }

    @Cancelable
    public static class Grass extends BiomeColorEvent
    {

    }

    @Cancelable
    public static class Foliage extends BiomeColorEvent
    {

    }


    @Cancelable
    public static class Water extends BiomeColorEvent
    {

    }

    @Cancelable
    public static class Lava extends BiomeColorEvent
    {

    }
}
