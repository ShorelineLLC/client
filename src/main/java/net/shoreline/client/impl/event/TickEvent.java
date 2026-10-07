package net.shoreline.client.impl.event;

import net.shoreline.eventbus.Event;

public class TickEvent extends Event
{
    public static class Pre extends TickEvent {}

    public static class Post extends TickEvent {}
}