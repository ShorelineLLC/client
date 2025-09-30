package net.shoreline.client.impl.event.render;

import lombok.Getter;
import lombok.Setter;
import net.shoreline.eventbus.Event;
import net.shoreline.eventbus.annotation.Cancelable;

@Cancelable
@Getter
@Setter
public class BlockLightEvent extends Event
{
    private int blockLight;
}
