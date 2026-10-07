package net.shoreline.client.impl.event.render.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.entity.Entity;
import net.shoreline.eventbus.Event;
import net.shoreline.eventbus.annotation.Cancelable;

@RequiredArgsConstructor
@Getter
@Cancelable
public class RenderEntityLabelEvent extends Event
{
    private final Entity entity;
}
