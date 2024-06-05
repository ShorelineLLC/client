package net.shoreline.client.impl.event;

import net.minecraft.entity.Entity;
import net.shoreline.eventbus.annotation.Cancelable;
import net.shoreline.eventbus.event.Event;

@Cancelable
public class EntityOutlineEvent extends Event
{
    private final Entity entity;

    public EntityOutlineEvent(Entity entity)
    {
        this.entity = entity;
    }

    public Entity getEntity()
    {
        return entity;
    }
}
