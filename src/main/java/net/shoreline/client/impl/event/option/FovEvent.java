package net.shoreline.client.impl.event.option;

import lombok.Getter;
import lombok.Setter;
import net.shoreline.eventbus.Event;
import net.shoreline.eventbus.annotation.Cancelable;

@Cancelable
@Getter
@Setter
public class FovEvent extends Event
{
    private double fov;
}
