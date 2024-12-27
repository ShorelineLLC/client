package net.shoreline.client.impl.event.render;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.shoreline.eventbus.annotation.Cancelable;
import net.shoreline.eventbus.event.Event;

@Cancelable
public class RenderEntityInWorldEvent extends Event
{
    private final Entity entityType;

    public RenderEntityInWorldEvent(Entity entityType)
    {
        this.entityType = entityType;
    }

    public Entity getEntity()
    {
        return entityType;
    }
}
