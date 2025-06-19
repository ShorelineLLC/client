package net.shoreline.client.mixin.render.entity;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.shoreline.client.impl.event.render.entity.PlayerTransformsEvent;
import net.shoreline.eventbus.EventBus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntityRenderer.class)
public class MixinPlayerEntityRenderer
{
    @Inject(method = "setupTransforms(Lnet/minecraft/client/render/entity/state/PlayerEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;FF)V",
            at = @At(value = "HEAD"))
    private void hookSetupTransforms(PlayerEntityRenderState playerEntityRenderState,
                                     MatrixStack matrixStack,
                                     float f,
                                     float g,
                                     CallbackInfo ci)
    {
        if (MinecraftClient.getInstance().player != null
                && playerEntityRenderState.id == MinecraftClient.getInstance().player.getId())
        {
            PlayerTransformsEvent event = new PlayerTransformsEvent();
            EventBus.INSTANCE.dispatch(event);
            if (event.isCanceled())
            {
                playerEntityRenderState.pitch = event.getPitch();
            }
        }
    }
}
