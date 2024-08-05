package net.shoreline.client.impl.event.render.entity;

import net.minecraft.entity.Entity;
import net.shoreline.eventbus.annotation.Cancelable;
import net.shoreline.eventbus.event.Event;

@Cancelable
public class ElytraTransformEvent extends Event
{
    private final Entity entity;

    public ElytraTransformEvent(Entity entity)
    {
        this.entity = entity;
    }

    public Entity getEntity()
    {
        return entity;
    }
}
