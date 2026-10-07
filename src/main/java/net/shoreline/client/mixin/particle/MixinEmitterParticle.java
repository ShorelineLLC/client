package net.shoreline.client.mixin.particle;

import net.minecraft.client.particle.EmitterParticle;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.util.math.Vec3d;
import net.shoreline.client.impl.event.particle.EmitParticleEvent;
import net.shoreline.eventbus.EventBus;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EmitterParticle.class)
public class MixinEmitterParticle
{
    @Mutable
    @Shadow
    @Final
    private int maxEmitterAge;

    @Shadow
    @Final
    private ParticleEffect parameters;

    @Inject(method = "<init>(Lnet/minecraft/client/world/ClientWorld;Lnet/minecraft/entity/Entity;Lnet/minecraft/particle/ParticleEffect;ILnet/minecraft/util/math/Vec3d;)V", at = @At(value = "TAIL"))
    private void hookInit(ClientWorld world,
                          Entity entity,
                          ParticleEffect parameters,
                          int g,
                          Vec3d velocity,
                          CallbackInfo ci)
    {
        EmitParticleEvent event = new EmitParticleEvent(parameters);
        EventBus.INSTANCE.dispatch(event);
        if (event.isCanceled())
        {
            maxEmitterAge = event.getMaxTicks();
        }
    }

    @ModifyConstant(method = "tick", constant = @Constant(intValue = 16))
    private int hookTickCount(int constant)
    {
        EmitParticleEvent event = new EmitParticleEvent(parameters);
        EventBus.INSTANCE.dispatch(event);
        return event.isCanceled() ? event.getMaxCount() : constant;
    }
}
