package net.shoreline.client.impl.event.render;

import net.shoreline.eventbus.event.Event;

public class LightmapUpdateEvent extends Event
{
    private final float tickDelta;

    public LightmapUpdateEvent(float tickDelta)
    {
        this.tickDelta = tickDelta;
    }

    public float getTickDelta()
    {
        return tickDelta;
    }
}
