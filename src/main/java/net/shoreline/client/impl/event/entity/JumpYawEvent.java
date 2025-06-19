package net.shoreline.client.impl.event.entity;

import lombok.Getter;
import lombok.Setter;
import net.shoreline.eventbus.annotation.Cancelable;
import net.shoreline.eventbus.Event;

@Cancelable
@Getter
@Setter
public class JumpYawEvent extends Event
{
    private float yaw;

    public JumpYawEvent(float yaw)
    {
        this.yaw = yaw;
    }
}
