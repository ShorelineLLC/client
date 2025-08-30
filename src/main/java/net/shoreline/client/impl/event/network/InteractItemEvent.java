package net.shoreline.client.impl.event.network;

import net.shoreline.eventbus.Event;

public class InteractItemEvent extends Event
{
    public static class Pre extends InteractItemEvent {}

    public static class Post extends InteractItemEvent {}
}
