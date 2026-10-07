package net.shoreline.client.impl.event.network;

import net.shoreline.eventbus.Event;

public class PlayerUpdateEvent extends Event
{
    public static class Pre extends PlayerUpdateEvent {}

    public static class Peri extends PlayerUpdateEvent {}

    public static class PrePacket extends PlayerUpdateEvent {}

    public static class Post extends PlayerUpdateEvent {}
}
