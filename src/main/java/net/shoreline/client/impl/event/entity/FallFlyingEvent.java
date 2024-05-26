package net.shoreline.client.impl.event.entity;

import net.shoreline.eventbus.Cancelable;
import net.shoreline.eventbus.Event;

@Cancelable
public class FallFlyingEvent extends Event
{
    private boolean isFallFlying;

    public FallFlyingEvent(boolean isFallFlying)
    {
        this.isFallFlying = isFallFlying;
    }

    public void setFallFlying(boolean fallFlying)
    {
        isFallFlying = fallFlying;
    }

    public boolean isFallFlying()
    {
        return isFallFlying;
    }
}
