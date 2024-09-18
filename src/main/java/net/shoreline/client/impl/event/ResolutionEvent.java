package net.shoreline.client.impl.event;

import net.minecraft.client.util.Window;
import net.shoreline.eventbus.event.Event;

public class ResolutionEvent extends Event
{
    private final Window window;

    public ResolutionEvent(Window window)
    {
        this.window = window;
    }

    public Window getWindow()
    {
        return window;
    }
}
