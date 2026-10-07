package net.shoreline.client.impl.event.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.shoreline.eventbus.Event;

@RequiredArgsConstructor
@Getter
public class EntityDamageEvent extends Event
{
    private final Entity entity;
    private final DamageSource damageSource;
    private final float amount;
}
