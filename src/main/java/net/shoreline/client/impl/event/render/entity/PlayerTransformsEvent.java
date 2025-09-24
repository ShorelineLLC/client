package net.shoreline.client.impl.event.render.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.shoreline.eventbus.Event;
import net.shoreline.eventbus.annotation.Cancelable;

@RequiredArgsConstructor
@Cancelable
@Getter
@Setter
public class PlayerTransformsEvent extends Event
{
    private final float tickDelta;
    private float yaw, pitch;
}
