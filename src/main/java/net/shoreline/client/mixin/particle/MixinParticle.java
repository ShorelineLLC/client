package net.shoreline.client.mixin.particle;

import net.minecraft.client.particle.Particle;
import net.minecraft.util.math.random.Random;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Particle.class)
public abstract class MixinParticle
{
    @Shadow @Final protected Random random;

    @Shadow
    public abstract void setColor(float red, float green, float blue);
}
