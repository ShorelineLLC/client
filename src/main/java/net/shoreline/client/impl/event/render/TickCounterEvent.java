package net.shoreline.client.impl.event.render;

import net.shoreline.eventbus.annotation.Cancelable;
import net.shoreline.eventbus.event.Event;

/**
 * @author linus
 * @since 1.0
 */
@Cancelable
public class TickCounterEvent extends Event
{
    //
    private float ticks;

    /**
     * @return
     */
    public float getTicks()
    {
        return ticks;
    }

    /**
     * @param ticks
     */
    public void setTicks(float ticks)
    {
        this.ticks = ticks;
    }
}
