package net.shoreline.client.impl.event.gui.hud;

import lombok.Getter;
import lombok.Setter;
import net.shoreline.eventbus.Event;
import net.shoreline.eventbus.annotation.Cancelable;

@Getter
@Setter
@Cancelable
public class RenderTabEvent extends Event
{
    private boolean pressed;
}