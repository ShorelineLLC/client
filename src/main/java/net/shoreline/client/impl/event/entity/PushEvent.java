package net.shoreline.client.impl.event.entity;

import net.shoreline.eventbus.Event;
import net.shoreline.eventbus.annotation.Cancelable;

public class PushEvent extends Event
{
    @Cancelable
    public static class Entity extends PushEvent {}

    @Cancelable
    public static class Liquid extends PushEvent {}
}
