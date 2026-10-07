package net.shoreline.client.impl.event.text;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.shoreline.eventbus.Event;
import net.shoreline.eventbus.annotation.Cancelable;

@AllArgsConstructor
@Cancelable
@Getter
@Setter
public class TextVisitedEvent extends Event
{
    private String text;
}
