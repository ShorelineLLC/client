package net.shoreline.client.impl.event.render.item;

import net.shoreline.eventbus.annotation.Cancelable;
import net.shoreline.eventbus.event.Event;

@Cancelable
public class EatTransformationEvent extends Event
{
    private float factor;

    public void setFactor(float factor)
    {
        this.factor = factor;
    }

    public float getFactor()
    {
        return factor;
    }
}
