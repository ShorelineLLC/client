package net.shoreline.client.impl.event.entity.player;

import lombok.Getter;
import lombok.Setter;
import net.shoreline.eventbus.Event;
import net.shoreline.eventbus.annotation.Cancelable;

@Cancelable
@Getter
@Setter
public class ReachEvent extends Event
{
    private double reach;
}
