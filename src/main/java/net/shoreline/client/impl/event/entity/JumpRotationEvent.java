package net.shoreline.client.impl.event.entity;

import net.shoreline.eventbus.annotation.Cancelable;
import net.shoreline.eventbus.event.Event;

@Cancelable
public final class JumpRotationEvent extends Event
{
    private float yaw;

    public JumpRotationEvent(float yaw)
    {
        this.yaw = yaw;
    }

    public float getYaw()
    {
        return yaw;
    }

    public void setYaw(float yaw)
    {
        this.yaw = yaw;
    }
}
