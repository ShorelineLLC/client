package net.shoreline.eventbus.event;

public class StageEvent extends Event
{
    private EventStage stage;

    public EventStage getStage()
    {
        return stage;
    }

    public void setStage(EventStage stage)
    {
        this.stage = stage;
    }

    public enum EventStage
    {
        PRE,
        POST
    }
}