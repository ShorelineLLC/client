package net.shoreline.client.impl.event.entity.player;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.shoreline.eventbus.Event;
import net.shoreline.eventbus.annotation.Cancelable;

@RequiredArgsConstructor
@Cancelable
@Getter
public class InsertStackEvent extends Event
{
    private final int slot;
}
