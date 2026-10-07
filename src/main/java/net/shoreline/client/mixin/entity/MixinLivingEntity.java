package net.shoreline.client.mixin.entity;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.registry.tag.TagKey;
import net.shoreline.client.impl.event.entity.*;
import net.shoreline.client.impl.imixin.ILivingEntity;
import net.shoreline.eventbus.EventBus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity implements ILivingEntity
{
    @Shadow
    private int jumpingCooldown;

    @Override
    @Accessor(value = "leaningPitch")
    public abstract void setLeaningPitch(float leaningPitch);

    @Override
    @Accessor(value = "lastLeaningPitch")
    public abstract void setLastLeaningPitch(float lastLeaningPitch);

    @Inject(method = "jump", at = @At(value = "HEAD"), cancellable = true)
    private void hookJumpPre(CallbackInfo ci)
    {
        if ((Object) this == MinecraftClient.getInstance().player)
        {
            PlayerJumpEvent.Pre event = new PlayerJumpEvent.Pre();
            EventBus.INSTANCE.dispatch(event);
            if (event.isCanceled())
            {
                ci.cancel();
            }
        }
    }

    @Inject(method = "jump", at = @At(value = "TAIL"))
    private void hookJumpPost(CallbackInfo ci)
    {
        if ((Object) this == MinecraftClient.getInstance().player)
        {
            PlayerJumpEvent.Post event = new PlayerJumpEvent.Post();
            EventBus.INSTANCE.dispatch(event);
        }
    }

    @ModifyExpressionValue(method = "jump", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;getYaw()F"))
    private float hookJumpYaw(float original)
    {
        if ((Object) this == MinecraftClient.getInstance().player)
        {
            PlayerJumpEvent.Yaw event = new PlayerJumpEvent.Yaw(original);
            EventBus.INSTANCE.dispatch(event);
            if (event.isCanceled())
            {
                return event.getYaw();
            }
        }

        return original;
    }

    @Inject(method = "tickMovement", at = @At(value = "HEAD"))
    private void hookTickMovement(CallbackInfo ci)
    {
        if ((Object) this == MinecraftClient.getInstance().player)
        {
            JumpDelayEvent jumpDelayEvent = new JumpDelayEvent();
            EventBus.INSTANCE.dispatch(jumpDelayEvent);
            if (jumpDelayEvent.isCanceled())
            {
                jumpingCooldown = 0;
            }
        }
    }

    @Inject(method = "getStepHeight", at = @At(value = "HEAD"), cancellable = true)
    private void hookGetStepHeight(CallbackInfoReturnable<Float> cir)
    {
        if ((Object) this == MinecraftClient.getInstance().player)
        {
            StepHeightEvent stepHeightEvent = new StepHeightEvent();
            EventBus.INSTANCE.dispatch(stepHeightEvent);
            if (stepHeightEvent.isCancelable())
            {
                cir.cancel();
                cir.setReturnValue(stepHeightEvent.getStepHeight());
            }
        }
    }

    @Inject(method = "getHandSwingDuration", at = @At("HEAD"), cancellable = true)
    private void hookGetHandSwingDuration(CallbackInfoReturnable<Integer> cir)
    {
        HandSwingDurationEvent swingSpeedEvent = new HandSwingDurationEvent();
        EventBus.INSTANCE.dispatch(swingSpeedEvent);
        if (swingSpeedEvent.isCanceled())
        {
            cir.cancel();
            cir.setReturnValue(swingSpeedEvent.getSwingDuration());
        }
    }

    @Redirect(method = "isClimbing",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/block/BlockState;isIn(Lnet/minecraft/registry/tag/TagKey;)Z"
            ))
    private boolean hookIsClimbing(BlockState instance, TagKey<Block> tagKey)
    {
        ClimbEvent event = new ClimbEvent(instance.getBlock());
        EventBus.INSTANCE.dispatch(event);
        if (event.isCanceled())
        {
            return false;
        }

        return instance.isIn(tagKey);
    }

    @Inject(method = "tryUseDeathProtector", at = @At(value = "HEAD"), cancellable = true)
    private void hookTryUseDeathProtector(DamageSource source, CallbackInfoReturnable<Boolean> cir)
    {
        if ((Object) this == MinecraftClient.getInstance().player)
        {
            DeathProtectionEvent deathProtectionEvent = new DeathProtectionEvent();
            EventBus.INSTANCE.dispatch(deathProtectionEvent);
            if (deathProtectionEvent.isCanceled())
            {
                cir.cancel();
            }
        }
    }
}
