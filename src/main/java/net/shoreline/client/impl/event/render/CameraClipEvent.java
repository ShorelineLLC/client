package net.shoreline.client.impl.event.render;

import net.shoreline.eventbus.Cancelable;
import net.shoreline.eventbus.Event;

/**
 * @author linus
 * @since 1.0
 */
@Cancelable
public class CameraClipEvent extends Event
{
    private double distance;

    public CameraClipEvent(double distance)
    {
        this.distance = distance;
    }

    public double getDistance()
    {
        return distance;
    }

    public void setDistance(double distance)
    {
        this.distance = distance;
    }
}
