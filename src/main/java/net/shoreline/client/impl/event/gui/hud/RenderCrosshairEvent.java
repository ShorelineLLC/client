package net.shoreline.client.impl.event.gui.hud;

import net.minecraft.client.gui.DrawContext;
import net.shoreline.eventbus.annotation.Cancelable;
import net.shoreline.eventbus.event.Event;

@Cancelable
public class RenderCrosshairEvent extends Event
{
    private final DrawContext context;

    public RenderCrosshairEvent(DrawContext context)
    {
        this.context = context;
    }

    public DrawContext getContext()
    {
        return context;
    }
}
