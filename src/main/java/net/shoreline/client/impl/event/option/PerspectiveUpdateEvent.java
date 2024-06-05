package net.shoreline.client.impl.event.option;

import net.minecraft.client.option.Perspective;
import net.shoreline.eventbus.event.Event;

public class PerspectiveUpdateEvent extends Event
{
    private final Perspective perspective;

    public PerspectiveUpdateEvent(Perspective perspective)
    {
        this.perspective = perspective;
    }

    public Perspective getPerspective()
    {
        return perspective;
    }
}
