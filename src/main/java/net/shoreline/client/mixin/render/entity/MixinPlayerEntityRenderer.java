package net.shoreline.client.mixin.render.entity;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.util.math.MathHelper;
import net.shoreline.client.impl.event.render.entity.PlayerTransformsEvent;
import net.shoreline.eventbus.EventBus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntityRenderer.class)
public class MixinPlayerEntityRenderer
{
    @Unique
    private float lastRenderYaw, lastRenderPitch;

    @Inject(method = "updateRenderState(Lnet/minecraft/client/network/AbstractClientPlayerEntity;Lnet/minecraft/client/render/entity/state/PlayerEntityRenderState;F)V", at = @At(value = "TAIL"))
    private void hookUpdateRenderState(AbstractClientPlayerEntity abstractClientPlayerEntity,
                                       PlayerEntityRenderState playerEntityRenderState,
                                       float f,
                                       CallbackInfo ci)
    {
        if (MinecraftClient.getInstance().player != null && abstractClientPlayerEntity.getId() == MinecraftClient.getInstance().player.getId())
        {
            PlayerTransformsEvent event = new PlayerTransformsEvent();
            EventBus.INSTANCE.dispatch(event);
            if (event.isCanceled())
            {
                float yaw = MathHelper.lerpAngleDegrees(f, lastRenderYaw, event.getYaw());
                float pitch = MathHelper.lerpAngleDegrees(f, lastRenderPitch, event.getPitch());
                playerEntityRenderState.bodyYaw = yaw;
                playerEntityRenderState.pitch = pitch;
                playerEntityRenderState.yawDegrees = 0.0f;
                lastRenderYaw = event.getYaw();
                lastRenderPitch = event.getPitch();
            }
        }
    }
}
