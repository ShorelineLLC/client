package net.shoreline.client.impl.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.shoreline.eventbus.Event;
import net.shoreline.eventbus.annotation.Cancelable;

@RequiredArgsConstructor
@Getter
@Cancelable
public class MouseEvent extends Event
{
    private final double cursorDeltaX, cursorDeltaY;
}
