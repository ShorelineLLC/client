package net.shoreline.client.impl.event.entity;

import net.shoreline.eventbus.Event;

public class StepEvent extends Event
{
    private final double stepHeight;

    public StepEvent(double stepHeight)
    {
        this.stepHeight = stepHeight;
    }

    public double getStepHeight()
    {
        return stepHeight;
    }
}
