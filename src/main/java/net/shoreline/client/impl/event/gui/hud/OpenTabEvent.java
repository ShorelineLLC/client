package net.shoreline.client.impl.event.gui.hud;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.shoreline.eventbus.Event;
import net.shoreline.eventbus.annotation.Cancelable;

@RequiredArgsConstructor
@Getter
@Cancelable
public class OpenTabEvent extends Event
{
    private final boolean visible;
}
