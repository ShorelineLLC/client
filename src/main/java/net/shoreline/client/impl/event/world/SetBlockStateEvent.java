package net.shoreline.client.impl.event.world;

import net.shoreline.eventbus.annotation.Cancelable;
import net.shoreline.eventbus.event.Event;

@Cancelable
public class SetBlockStateEvent extends Event
{
    private final int flags;

    public SetBlockStateEvent(int flags)
    {
        this.flags = flags;
    }

    public int getFlags()
    {
        return flags;
    }
}
