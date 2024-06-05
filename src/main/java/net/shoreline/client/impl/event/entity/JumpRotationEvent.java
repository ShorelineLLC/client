package net.shoreline.client.impl.event.entity;

import net.shoreline.eventbus.event.Event;

public final class JumpRotationEvent extends Event
{
    private float yaw;


    public float getYaw()
    {
        return yaw;
    }

    public void setYaw(float yaw)
    {
        this.yaw = yaw;
    }
}
