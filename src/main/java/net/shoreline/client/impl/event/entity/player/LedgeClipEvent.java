package net.shoreline.client.impl.event.entity.player;

import net.shoreline.eventbus.annotation.Cancelable;
import net.shoreline.eventbus.event.Event;

@Cancelable
public class LedgeClipEvent extends Event
{
    private boolean clip;

    public void setClipped(boolean clip)
    {
        this.clip = clip;
    }

    public boolean isClipped()
    {
        return clip;
    }
}
