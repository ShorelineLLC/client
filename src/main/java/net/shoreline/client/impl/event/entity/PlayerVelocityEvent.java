package net.shoreline.client.impl.event.entity;

import lombok.Getter;
import lombok.Setter;
import net.shoreline.eventbus.Event;
import net.shoreline.eventbus.annotation.Cancelable;

@Cancelable
@Getter
@Setter
public class PlayerVelocityEvent extends Event
{
    private float yaw;
}
