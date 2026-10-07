package net.shoreline.client.impl.combat;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.entity.Entity;
import net.shoreline.eventbus.Event;

@RequiredArgsConstructor
@Getter
public class TotemPopEvent extends Event
{
    private final Entity entity;
    private final int pops;
}
