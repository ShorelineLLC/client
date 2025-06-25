package net.shoreline.client.mixin.entity;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import net.shoreline.client.impl.event.entity.PlayerVelocityEvent;
import net.shoreline.client.impl.event.entity.PushEvent;
import net.shoreline.eventbus.EventBus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class MixinEntity
{
    @Shadow
    protected static Vec3d movementInputToVelocity(Vec3d movementInput, float speed, float yaw)
    {
        return null;
    }

    @Shadow
    public abstract Vec3d getVelocity();

    @Shadow
    public abstract void setVelocity(Vec3d velocity);

    @Inject(method = "updateVelocity", at = @At(value = "HEAD"), cancellable = true)
    private void hookUpdateVelocity(float speed, Vec3d movementInput, CallbackInfo ci)
    {
        if ((Object) this == MinecraftClient.getInstance().player)
        {
            final PlayerVelocityEvent event = new PlayerVelocityEvent();
            EventBus.INSTANCE.dispatch(event);
            if (event.isCanceled())
            {
                ci.cancel();
                Vec3d vec3d = movementInputToVelocity(movementInput, speed, event.getYaw());
                setVelocity(getVelocity().add(vec3d));
            }
        }
    }

    @Inject(method = "pushAwayFrom", at = @At(value = "HEAD"), cancellable = true)
    private void hookPushAwayFrom(Entity entity, CallbackInfo ci)
    {
        if ((Object) this == MinecraftClient.getInstance().player)
        {
            PushEvent.Entity event = new PushEvent.Entity();
            EventBus.INSTANCE.dispatch(event);
            if (event.isCanceled())
            {
                ci.cancel();
            }
        }
    }

    @Inject(method = "isPushedByFluids", at = @At(value = "HEAD"), cancellable = true)
    private void hookIsPushedByFluids(CallbackInfoReturnable<Boolean> cir)
    {
        if ((Object) this == MinecraftClient.getInstance().player)
        {
            PushEvent.Liquid event = new PushEvent.Liquid();
            EventBus.INSTANCE.dispatch(event);
            if (event.isCanceled())
            {
                cir.setReturnValue(false);
                cir.cancel();
            }
        }
    }
}
