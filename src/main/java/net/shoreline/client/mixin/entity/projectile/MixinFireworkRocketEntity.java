package net.shoreline.client.mixin.entity.projectile;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;
import net.shoreline.client.impl.event.entity.FireworkTickEvent;
import net.shoreline.client.impl.event.entity.projectile.FireworkVelocityEvent;
import net.shoreline.client.impl.event.entity.projectile.RemoveFireworkEvent;
import net.shoreline.client.util.Globals;
import net.shoreline.eventbus.EventBus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author linus
 * @since 1.0
 */
@Mixin(FireworkRocketEntity.class)
public class MixinFireworkRocketEntity implements Globals
{
    //
    @Shadow
    private int life;

    @Shadow
    private int lifeTime;

    @Inject(method = "tick", at = @At(value = "HEAD"), cancellable = true)
    private void hookTick(CallbackInfo ci)
    {
        FireworkTickEvent event = new FireworkTickEvent((FireworkRocketEntity) (Object) this);
        EventBus.INSTANCE.dispatch(event);
        if (event.isCanceled())
        {
            ci.cancel();
        }
    }

    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;setVelocity(Lnet/minecraft/util/math/Vec3d;)V"))
    private void hookSetVelocity(LivingEntity entity, Vec3d velocity)
    {
        if (entity instanceof ClientPlayerEntity)
        {
            FireworkVelocityEvent fireworkVelocityEvent = new FireworkVelocityEvent();
            EventBus.INSTANCE.dispatch(fireworkVelocityEvent);
            if (!fireworkVelocityEvent.isCanceled())
            {
                entity.setVelocity(velocity);
            }
        }
    }

    /**
     * @param ci
     */
    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/projectile/" +
            "FireworkRocketEntity;updateRotation()V", shift = At.Shift.AFTER), cancellable = true)
    private void hookTickPre(CallbackInfo ci)
    {
        if (life <= lifeTime)
        {
            return;
        }
        FireworkRocketEntity rocketEntity = ((FireworkRocketEntity) (Object) this);
        RemoveFireworkEvent removeFireworkEvent = new RemoveFireworkEvent(rocketEntity);
        EventBus.INSTANCE.dispatch(removeFireworkEvent);
        if (removeFireworkEvent.isCanceled())
        {
            ci.cancel();
            if (life == 0 && !rocketEntity.isSilent())
            {
                mc.world.playSound(null, rocketEntity.getX(), rocketEntity.getY(), rocketEntity.getZ(), SoundEvents.ENTITY_FIREWORK_ROCKET_LAUNCH, SoundCategory.AMBIENT, 3.0f, 1.0f);
            }
            ++life;
            if (mc.world.isClient && life % 2 < 2)
            {
                mc.world.addParticle(ParticleTypes.FIREWORK, rocketEntity.getX(), rocketEntity.getY(), rocketEntity.getZ(), mc.world.random.nextGaussian() * 0.05, -rocketEntity.getVelocity().y * 0.5, mc.world.random.nextGaussian() * 0.05);
            }
        }
    }
}
