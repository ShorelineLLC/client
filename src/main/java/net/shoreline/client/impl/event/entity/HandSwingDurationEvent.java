package net.shoreline.client.impl.event.entity;

import lombok.Getter;
import lombok.Setter;
import net.shoreline.eventbus.Event;
import net.shoreline.eventbus.annotation.Cancelable;

@Getter
@Setter
@Cancelable
public class HandSwingDurationEvent extends Event
{
    private int swingDuration;
}
