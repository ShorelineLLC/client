package net.shoreline.client.impl.event.particle;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.minecraft.particle.ParticleEffect;
import net.shoreline.eventbus.Event;
import net.shoreline.eventbus.annotation.Cancelable;

@RequiredArgsConstructor
@Cancelable
@Getter
@Setter
public class EmitParticleEvent extends Event
{
    private final ParticleEffect effect;
    private int maxTicks;
    private int maxCount;
}
