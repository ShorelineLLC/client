package net.shoreline.client.impl.event.network;

import net.shoreline.eventbus.Event;
import net.shoreline.eventbus.annotation.Cancelable;

public class MovementFactorEvent extends Event
{
    @Cancelable
    public static class Item extends MovementFactorEvent {}

    @Cancelable
    public static class Slowdown extends MovementFactorEvent {}
}
