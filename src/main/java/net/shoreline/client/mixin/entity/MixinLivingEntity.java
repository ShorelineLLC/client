package net.shoreline.client.mixin.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.shoreline.client.impl.event.entity.*;
import net.shoreline.client.util.Globals;
import net.shoreline.eventbus.EventBus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * @author linus
 * @since 1.0
 */
@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity extends MixinEntity implements Globals
{
    //
    @Shadow
    protected ItemStack activeItemStack;

    /**
     * @param effect
     * @return
     */
    @Shadow
    public abstract boolean hasStatusEffect(StatusEffect effect);

    @Shadow
    public abstract float getYaw(float tickDelta);

    @Shadow
    protected abstract float getJumpVelocity();

    @Shadow
    private int jumpingCooldown;

    @Inject(method = "getHandSwingDuration", at = @At("HEAD"), cancellable = true)
    private void hookGetHandSwingDuration(CallbackInfoReturnable<Integer> cir)
    {
        SwingSpeedEvent swingSpeedEvent = new SwingSpeedEvent();
        EventBus.INSTANCE.dispatch(swingSpeedEvent);
        if (swingSpeedEvent.isCanceled())
        {
            cir.cancel();
            cir.setReturnValue(swingSpeedEvent.getSwingSpeed());
        }
    }

    @Inject(method = "jump", at = @At(value = "HEAD"), cancellable = true)
    private void hookJump$getYaw(CallbackInfo ci)
    {
        if ((Object) this != mc.player)
        {
            return;
        }
        final JumpRotationEvent event = new JumpRotationEvent();
        EventBus.INSTANCE.dispatch(event);
        if (event.isCanceled())
        {
            ci.cancel();
            Vec3d vec3d = this.getVelocity();
            setVelocity(new Vec3d(vec3d.x, getJumpVelocity(), vec3d.z));
            if (isSprinting())
            {
                float f = event.getYaw() * ((float) Math.PI / 180);
                setVelocity(getVelocity().add(-MathHelper.sin(f) * 0.2f, 0.0, MathHelper.cos(f) * 0.2f));
            }
            velocityDirty = true;
        }
    }

    /**
     * @param instance
     * @param effect
     * @return
     */
    @Redirect(method = "travel", at = @At(value = "INVOKE", target = "Lnet/" +
            "minecraft/entity/LivingEntity;hasStatusEffect(Lnet/minecraft/" +
            "entity/effect/StatusEffect;)Z"))
    private boolean hookHasStatusEffect(LivingEntity instance, StatusEffect effect)
    {
        if (instance.equals(mc.player))
        {
            LevitationEvent levitationEvent = new LevitationEvent();
            EventBus.INSTANCE.dispatch(levitationEvent);
            return !levitationEvent.isCanceled() && hasStatusEffect(effect);
        }
        return hasStatusEffect(effect);
    }

    /**
     * @param ci
     */
    @Inject(method = "consumeItem", at = @At(value = "INVOKE", target = "Lnet/" +
            "minecraft/item/ItemStack;finishUsing(Lnet/minecraft/world/World;" +
            "Lnet/minecraft/entity/LivingEntity;)" +
            "Lnet/minecraft/item/ItemStack;", shift = At.Shift.AFTER))
    private void hookConsumeItem(CallbackInfo ci)
    {
        if ((Object) this != mc.player)
        {
            return;
        }
        ConsumeItemEvent consumeItemEvent = new ConsumeItemEvent(activeItemStack);
        EventBus.INSTANCE.dispatch(consumeItemEvent);
    }

    @Inject(method = "tickMovement", at = @At(value = "HEAD"), cancellable = true)
    private void hookTickMovement(CallbackInfo ci)
    {
        JumpDelayEvent jumpDelayEvent = new JumpDelayEvent();
        EventBus.INSTANCE.dispatch(jumpDelayEvent);
        if (jumpDelayEvent.isCanceled())
        {
            jumpingCooldown = 0;
        }
    }

    @Inject(method = "onStatusEffectApplied", at = @At(value = "HEAD"))
    private void hookAddStatusEffect(StatusEffectInstance effect, Entity source, CallbackInfo ci)
    {
        if ((Object) this != mc.player)
        {
            return;
        }
        StatusEffectEvent.Add statusEffectEvent = new StatusEffectEvent.Add(effect);
        EventBus.INSTANCE.dispatch(statusEffectEvent);
    }

    @Inject(method = "onStatusEffectRemoved", at = @At(value = "HEAD"))
    private void hookRemoveStatusEffect(StatusEffectInstance effect, CallbackInfo ci)
    {
        if ((Object) this != mc.player)
        {
            return;
        }
        StatusEffectEvent.Remove statusEffectEvent = new StatusEffectEvent.Remove(effect);
        EventBus.INSTANCE.dispatch(statusEffectEvent);
    }

    @Inject(method = "isFallFlying", at = @At("TAIL"), cancellable = true)
    public void hookIsFallFlying(CallbackInfoReturnable<Boolean> cir)
    {
        FallFlyingEvent fallFlyingEvent = new FallFlyingEvent(cir.getReturnValueZ());
        EventBus.INSTANCE.dispatch(fallFlyingEvent);
        if (fallFlyingEvent.isCanceled())
        {
            cir.cancel();
            cir.setReturnValue(fallFlyingEvent.isFallFlying());
        }
    }

    @Inject(method = "applyDamage", at = @At(value = "HEAD"))
    private void hookDamage(DamageSource source, float amount, CallbackInfo ci)
    {
        // Idk this doesnt work with direct checks??
        if (source.getAttacker() != null && source.getAttacker().getName().getString().equalsIgnoreCase(mc.player.getName().getString()))
        {
            PlayerDamageEvent playerDamageEvent = new PlayerDamageEvent((LivingEntity) (Object) this);
            EventBus.INSTANCE.dispatch(playerDamageEvent);
        }
    }

    @Inject(method = "updateTrackedPositionAndAngles", at = @At(value = "HEAD"))
    private void hookUpdateTrackedPositionAndAngles(double x, double y, double z, float yaw, float pitch, int interpolationSteps, CallbackInfo ci)
    {
        UpdateServerPositionEvent updateServerPositionEvent = new UpdateServerPositionEvent((LivingEntity) (Object) this, x, y, z, yaw, pitch);
        EventBus.INSTANCE.dispatch(updateServerPositionEvent);
    }

    @Inject(method = "travel", at = @At(value = "HEAD"), cancellable = true)
    private void hookTravel(Vec3d movementInput, CallbackInfo ci)
    {
        EntityTravelEvent entityTravelEvent = new EntityTravelEvent((LivingEntity) (Object) this);
        EventBus.INSTANCE.dispatch(entityTravelEvent);
        if (entityTravelEvent.isCanceled())
        {
            ci.cancel();
        }
    }
}