package net.shoreline.client.mixin.entity;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import net.shoreline.client.impl.event.entity.HandSwingDurationEvent;
import net.shoreline.client.impl.event.entity.JumpDelayEvent;
import net.shoreline.client.impl.event.entity.PlayerJumpEvent;
import net.shoreline.eventbus.EventBus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class MixinLivingEntity
{
    @Shadow
    private int jumpingCooldown;

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
}
