package net.shoreline.client.impl.event;

import net.shoreline.eventbus.Event;

public class WorldEvent extends Event
{
    public static class Join extends WorldEvent {}

    public static class Disconnect extends WorldEvent {}
}
