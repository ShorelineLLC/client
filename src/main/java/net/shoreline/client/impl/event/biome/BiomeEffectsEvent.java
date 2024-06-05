package net.shoreline.client.impl.event.biome;

import net.minecraft.world.biome.BiomeParticleConfig;
import net.shoreline.eventbus.annotation.Cancelable;
import net.shoreline.eventbus.event.Event;

@Cancelable
public class BiomeEffectsEvent extends Event
{

    private BiomeParticleConfig particleConfig;

    public BiomeParticleConfig getParticleConfig()
    {
        return particleConfig;
    }

    public void setParticleConfig(BiomeParticleConfig particleConfig)
    {
        this.particleConfig = particleConfig;
    }
}
