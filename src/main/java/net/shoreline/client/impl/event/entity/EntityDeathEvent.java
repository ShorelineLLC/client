package net.shoreline.client.impl.event.entity;

import lombok.Getter;
import net.minecraft.entity.Entity;
import net.shoreline.eventbus.Event;

public class EntityDeathEvent extends Event
{
    @Getter
    private final Entity entity;

    public EntityDeathEvent(Entity entity)
    {
        this.entity = entity;
    }
}