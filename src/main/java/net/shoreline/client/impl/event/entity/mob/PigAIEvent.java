package net.shoreline.client.impl.event.entity.mob;

import net.minecraft.entity.passive.PigEntity;
import net.shoreline.eventbus.Cancelable;
import net.shoreline.eventbus.Event;

@Cancelable
public class PigAIEvent extends Event
{
    private final PigEntity pigEntity;

    public PigAIEvent(PigEntity pigEntity)
    {
        this.pigEntity = pigEntity;
    }

    public PigEntity getPigEntity()
    {
        return pigEntity;
    }
}
