package net.shoreline.client.impl.event.render;

import lombok.Getter;
import lombok.Setter;
import net.shoreline.eventbus.Event;
import net.shoreline.eventbus.annotation.Cancelable;

@Getter
@Setter
public class EntityLightEvent extends Event
{
    private int light;

    @Cancelable
    public static class Block extends EntityLightEvent {}

    @Cancelable
    public static class Skylight extends EntityLightEvent {}
}
