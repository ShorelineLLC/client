package net.shoreline.client.impl.event.render;

import net.shoreline.eventbus.annotation.Cancelable;
import net.shoreline.eventbus.event.Event;

@Cancelable
public class FovEvent extends Event
{
    private double fov;

    public double getFov()
    {
        return fov;
    }

    public void setFov(double fov)
    {
        this.fov = fov;
    }
}
