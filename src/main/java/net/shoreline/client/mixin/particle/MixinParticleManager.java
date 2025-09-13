package net.shoreline.client.mixin.particle;

import net.minecraft.block.BlockState;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.shoreline.client.impl.event.particle.BlockBreakParticleEvent;
import net.shoreline.client.impl.event.particle.ParticleEvent;
import net.shoreline.eventbus.EventBus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ParticleManager.class)
public class MixinParticleManager
{
    @Inject(method = "tickParticle", at = @At(value = "HEAD"), cancellable = true)
    private void hookTickParticle(Particle particle, CallbackInfo ci)
    {
        ci.cancel();

        try
        {
            particle.tick();
        } catch (Throwable throwable)
        {
            particle.markDead();
        }
    }

    @Inject(method = "addBlockBreakParticles", at = @At(value = "HEAD"), cancellable = true)
    private void hookAddBlockBreakParticles(BlockPos pos, BlockState state, CallbackInfo ci)
    {
        BlockBreakParticleEvent event = new BlockBreakParticleEvent();
        EventBus.INSTANCE.dispatch(event);
        if (event.isCanceled())
        {
            ci.cancel();
        }
    }

    @Inject(method = "addBlockBreakingParticles", at = @At(value = "HEAD"), cancellable = true)
    private void hookAddBlockBreakingParticles(BlockPos pos, Direction direction, CallbackInfo ci)
    {
        BlockBreakParticleEvent event = new BlockBreakParticleEvent();
        EventBus.INSTANCE.dispatch(event);
        if (event.isCanceled())
        {
            ci.cancel();
        }
    }
}
