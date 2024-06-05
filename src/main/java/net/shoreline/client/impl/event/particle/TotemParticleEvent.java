package net.shoreline.client.impl.event.particle;

import net.shoreline.eventbus.annotation.Cancelable;
import net.shoreline.eventbus.event.Event;

import java.awt.*;

@Cancelable
public class TotemParticleEvent extends Event
{
    private Color color;

    public Color getColor()
    {
        return color;
    }

    public void setColor(Color color)
    {
        this.color = color;
    }
}
