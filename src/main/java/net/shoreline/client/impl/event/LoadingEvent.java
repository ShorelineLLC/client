package net.shoreline.client.impl.event;

import net.shoreline.eventbus.Event;

public class LoadingEvent extends Event
{
    public static class Finished extends LoadingEvent {}
}
