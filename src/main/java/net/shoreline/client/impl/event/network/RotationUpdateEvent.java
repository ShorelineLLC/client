package net.shoreline.client.impl.event.network;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.shoreline.eventbus.Event;

@RequiredArgsConstructor
@Getter
public class RotationUpdateEvent extends Event
{
    private final float yaw, pitch;
}
