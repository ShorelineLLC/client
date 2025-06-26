package net.shoreline.client.impl.event.network;

import lombok.Getter;
import lombok.Setter;
import net.shoreline.eventbus.Event;
import net.shoreline.eventbus.annotation.Cancelable;

@Cancelable
@Getter
@Setter
public class TickMovementEvent extends Event
{
    private int iterations;
}
