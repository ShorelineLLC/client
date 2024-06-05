package net.shoreline.client.impl.event.entity.projectile;

import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.shoreline.eventbus.annotation.Cancelable;
import net.shoreline.eventbus.event.Event;

@Cancelable
public class RemoveFireworkEvent extends Event
{
    private final FireworkRocketEntity rocketEntity;

    public RemoveFireworkEvent(FireworkRocketEntity rocketEntity)
    {
        this.rocketEntity = rocketEntity;
    }

    public FireworkRocketEntity getRocketEntity()
    {
        return rocketEntity;
    }
}
