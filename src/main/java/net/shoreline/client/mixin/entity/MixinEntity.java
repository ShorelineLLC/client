package net.shoreline.client.mixin.entity;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.util.math.Vec3d;
import net.shoreline.client.impl.event.entity.*;
import net.shoreline.client.impl.event.network.MovementFactorEvent;
import net.shoreline.client.impl.imixin.IEntity;
import net.shoreline.eventbus.EventBus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class MixinEntity implements IEntity
{
    @Override
    @Invoker("unsetRemoved")
    public abstract void invokeUnsetRemoved();

    @Shadow
    protected static Vec3d movementInputToVelocity(Vec3d movementInput, float speed, float yaw) { return null; }

    @Shadow
    public abstract Vec3d getVelocity();

    @Shadow
    public abstract void setVelocity(Vec3d velocity);

    @Shadow
    public abstract Vec3d getPos();

    @Shadow
    protected Vec3d movementMultiplier;

    @Inject(method = "getRotationVec", at = @At(value = "RETURN"), cancellable = true)
    public void hookGetRotationVec(final float tickDelta,
                                   final CallbackInfoReturnable<Vec3d> info)
    {
        if ((Object) this == MinecraftClient.getInstance().player)
        {
            PlayerVecEvent.Rotation event = new PlayerVecEvent.Rotation(tickDelta, info.getReturnValue());
            EventBus.INSTANCE.dispatch(event);
            if (event.isCanceled())
            {
                info.setReturnValue(event.getVec());
            }
        }
    }

    @Inject(method = "getCameraPosVec", at = @At("RETURN"), cancellable = true)
    public void hookGetCameraPosVec(float tickDelta,
                                    CallbackInfoReturnable<Vec3d> cir)
    {
        if ((Object) this == MinecraftClient.getInstance().player)
        {
            PlayerVecEvent.Camera event = new PlayerVecEvent.Camera(tickDelta, cir.getReturnValue());
            EventBus.INSTANCE.dispatch(event);
            if (event.isCanceled())
            {
                cir.setReturnValue(event.getVec());
            }
        }
    }

    @Inject(method = "updateVelocity", at = @At(value = "HEAD"), cancellable = true)
    private void hookUpdateVelocity(float speed,
                                    Vec3d movementInput,
                                    CallbackInfo ci)
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
    private void hookPushAwayFrom(Entity entity,
                                  CallbackInfo ci)
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

    @Inject(method = "sidedDamage", at = @At(value = "RETURN"))
    private void hookSidedDamage(DamageSource source,
                                 float amount,
                                 CallbackInfoReturnable<Boolean> cir)
    {
        EntityDamageEvent event = new EntityDamageEvent((Entity) (Object) this, source, amount);
        EventBus.INSTANCE.dispatch(event);
    }

    @Inject(method = "doesRenderOnFire", at = @At(value = "HEAD"), cancellable = true)
    private void hookDoesRenderOnFire(CallbackInfoReturnable<Boolean> cir)
    {
        RenderOnFireEvent renderFireEntityEvent = new RenderOnFireEvent();
        EventBus.INSTANCE.dispatch(renderFireEntityEvent);
        if (renderFireEntityEvent.isCanceled())
        {
            cir.cancel();
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "slowMovement", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/entity/Entity;onLanding()V",
            shift = At.Shift.AFTER), cancellable = true)
    private void hookSlowMovement(BlockState state, Vec3d multiplier, CallbackInfo ci)
    {
        if ((Object) this == MinecraftClient.getInstance().player)
        {
            SlowMovementEvent slowMovementEvent = new SlowMovementEvent(state);
            EventBus.INSTANCE.dispatch(slowMovementEvent);
            if (slowMovementEvent.isCanceled())
            {
                ci.cancel();
                movementMultiplier = slowMovementEvent.getMultiplier();
            }
        }
    }

    @Redirect(method = "getVelocityMultiplier",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/block/Block;getVelocityMultiplier()F"))
    private float hookGetVelocityMultiplier(Block instance)
    {
        if ((Object) this == MinecraftClient.getInstance().player)
        {
            SlowMovementEvent.Block velocityMultiplierEvent =
                    new SlowMovementEvent.Block(instance);
            EventBus.INSTANCE.dispatch(velocityMultiplierEvent);
            if (velocityMultiplierEvent.isCanceled())
            {
                return 1.0f;
            }
        }

        return instance.getVelocityMultiplier();
    }
}
