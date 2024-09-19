package net.shoreline.client.impl.event.particle;

import net.minecraft.particle.ParticleEffect;
import net.shoreline.eventbus.annotation.Cancelable;
import net.shoreline.eventbus.event.Event;

@Cancelable
public class EmitParticleEvent extends Event
{
    private final ParticleEffect effect;
    private int particleCount;
    private int particleTime;

    public EmitParticleEvent(ParticleEffect effect, int particleCount, int particleTime)
    {
        this.effect = effect;
        this.particleCount = particleCount;
        this.particleTime = particleTime;
    }

    public ParticleEffect getParticleType()
    {
        return effect;
    }

    public int getParticleCount()
    {
        return particleCount;
    }

    public void setParticleCount(int particleCount)
    {
        this.particleCount = particleCount;
    }

    public int getParticleTime()
    {
        return particleTime;
    }

    public void setParticleTime(int particleTime)
    {
        this.particleTime = particleTime;
    }
}
