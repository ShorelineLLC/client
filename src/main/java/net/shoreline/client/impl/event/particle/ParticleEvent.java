package net.shoreline.client.impl.event.particle;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.particle.ParticleType;
import net.shoreline.eventbus.Event;
import net.shoreline.eventbus.annotation.Cancelable;

@RequiredArgsConstructor
@Cancelable
@Getter
public class ParticleEvent extends Event
{
    private final ParticleType<?> particleType;
}
