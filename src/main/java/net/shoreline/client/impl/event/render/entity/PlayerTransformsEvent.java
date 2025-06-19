package net.shoreline.client.impl.event.render.entity;

import lombok.Getter;
import lombok.Setter;
import net.shoreline.eventbus.Event;
import net.shoreline.eventbus.annotation.Cancelable;

@Cancelable
@Getter
@Setter
public class PlayerTransformsEvent extends Event
{
    private float pitch;
}
