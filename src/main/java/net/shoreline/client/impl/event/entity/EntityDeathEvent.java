package net.shoreline.client.impl.event.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.entity.Entity;
import net.shoreline.eventbus.Event;

@RequiredArgsConstructor
@Getter
public class EntityDeathEvent extends Event
{
    private final Entity entity;
    private final int pops;
}