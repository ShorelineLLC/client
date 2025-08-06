package net.shoreline.client.impl.event.render.item;

import net.shoreline.eventbus.Event;
import net.shoreline.eventbus.annotation.Cancelable;

public class RenderHeldItemEvent extends Event
{
    @Cancelable
    public static class Pre extends RenderHeldItemEvent {}
}
