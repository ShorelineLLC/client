package net.shoreline.client.impl.event.network;

import net.shoreline.eventbus.annotation.Cancelable;
import net.shoreline.eventbus.event.Event;

/**
 * @author linus
 * @since 1.0
 */
@Cancelable
public class TickMovementEvent extends Event
{
    //
    private int iterations;

    /**
     * @return
     */
    public int getIterations()
    {
        return iterations;
    }

    /**
     * @param iterations
     */
    public void setIterations(int iterations)
    {
        this.iterations = iterations;
    }
}
