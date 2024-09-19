package net.shoreline.client.mixin.particle;

import net.minecraft.client.particle.Particle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Particle.class)
public abstract class MixinParticle
{

    @Shadow
    public abstract void markDead();

    @Shadow
    public abstract void setColor(float red, float green, float blue);
}
