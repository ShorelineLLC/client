package net.shoreline.eventbus.event;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StageEvent extends Event
{
    private EventStage stage;

    public enum EventStage
    {
        PRE,
        POST
    }
}