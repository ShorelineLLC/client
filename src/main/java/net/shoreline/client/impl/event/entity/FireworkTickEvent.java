package net.shoreline.client.impl.event.entity;

import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.shoreline.eventbus.annotation.Cancelable;
import net.shoreline.eventbus.event.Event;

@Cancelable
public class FireworkTickEvent extends Event
{
     private final FireworkRocketEntity fireworkRocketEntity;

     public FireworkTickEvent(FireworkRocketEntity fireworkRocketEntity)
     {
         this.fireworkRocketEntity = fireworkRocketEntity;
     }

    public FireworkRocketEntity getFireworkRocketEntity()
    {
        return fireworkRocketEntity;
    }
}
