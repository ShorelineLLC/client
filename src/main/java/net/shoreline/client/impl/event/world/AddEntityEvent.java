package net.shoreline.client.impl.event.world;

import net.minecraft.entity.Entity;
import net.shoreline.eventbus.event.Event;

public class AddEntityEvent extends Event
{
    private final Entity entity;

    public AddEntityEvent(Entity entity)
    {
        this.entity = entity;
    }

    public Entity getEntity()
    {
        return entity;
    }
}
