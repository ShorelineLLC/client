package net.shoreline.client.impl.event;

import net.shoreline.eventbus.annotation.Cancelable;
import net.shoreline.eventbus.event.Event;

@Cancelable
public class MouseUpdateEvent extends Event
{

    private final double cursorDeltaX;
    private final double cursorDeltaY;

    public MouseUpdateEvent(double cursorDeltaX, double cursorDeltaY)
    {
        this.cursorDeltaX = cursorDeltaX;
        this.cursorDeltaY = cursorDeltaY;
    }

    public double getCursorDeltaX()
    {
        return cursorDeltaX;
    }

    public double getCursorDeltaY()
    {
        return cursorDeltaY;
    }
}

