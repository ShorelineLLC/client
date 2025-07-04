package net.shoreline.client.mixin.particle;

import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.particle.ParticleEffect;
import net.shoreline.client.impl.event.particle.ParticleEvent;
import net.shoreline.eventbus.EventBus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ParticleManager.class)
public class MixinParticleManager
{
    @Inject(method = "addParticle(Lnet/minecraft/particle/ParticleEffect;DDDDDD)Lnet/minecraft/client/particle/Particle;",
            at = @At(value = "HEAD"), cancellable = true)
    private void hookAddParticle(ParticleEffect parameters,
                                 double x,
                                 double y,
                                 double z,
                                 double velocityX,
                                 double velocityY,
                                 double velocityZ,
                                 CallbackInfoReturnable<Particle> cir)
    {
        ParticleEvent particleEvent = new ParticleEvent(parameters);
        EventBus.INSTANCE.dispatch(particleEvent);
        if (particleEvent.isCanceled())
        {
            cir.setReturnValue(null);
            cir.cancel();
        }
    }
}
