package net.shoreline.client.impl.event.network;

import net.shoreline.eventbus.annotation.Cancelable;
import net.shoreline.eventbus.event.Event;

@Cancelable
public class ReachEvent extends Event
{
    private double reach;

    public double getReach()
    {
        return reach;
    }

    public void setReach(double reach)
    {
        this.reach = reach;
    }

    @Cancelable
    public static class Block extends ReachEvent
    {

    }

    @Cancelable
    public static class Entity extends ReachEvent
    {

    }
}
