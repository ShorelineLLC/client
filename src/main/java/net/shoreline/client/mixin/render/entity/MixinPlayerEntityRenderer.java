package net.shoreline.client.mixin.render.entity;

import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.shoreline.client.api.event.handler.EventBus;
import net.shoreline.client.impl.event.render.entity.RenderPlayerEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author linus
 * @see PlayerEntityRenderer
 * @since 1.0
 */
@Mixin(PlayerEntityRenderer.class)
public class MixinPlayerEntityRenderer {
    //
    @Unique
    private float yaw, prevYaw, bodyYaw, prevBodyYaw, headYaw, prevHeadYaw;
    @Unique
    private float pitch, prevPitch;

    /**
     * @param abstractClientPlayerEntity
     * @param f
     * @param g
     * @param matrixStack
     * @param vertexConsumerProvider
     * @param i
     * @param ci
     */
    @Inject(method = "render(Lnet/minecraft/client/network/" +
            "AbstractClientPlayerEntity;FFLnet/minecraft/client/util/" +
            "math/MatrixStack;Lnet/minecraft/client/render " +
            "/VertexConsumerProvider;I)V", at = @At(value = "HEAD"))
    private void onRenderHead(AbstractClientPlayerEntity abstractClientPlayerEntity,
                              float f, float g, MatrixStack matrixStack,
                              VertexConsumerProvider vertexConsumerProvider,
                              int i, CallbackInfo ci) {
        final RenderPlayerEvent renderPlayerEvent =
                new RenderPlayerEvent(abstractClientPlayerEntity);
        EventBus.EVENT_HANDLER.dispatch(renderPlayerEvent);
        yaw = abstractClientPlayerEntity.getYaw();
        prevYaw = abstractClientPlayerEntity.prevYaw;
        bodyYaw = abstractClientPlayerEntity.bodyYaw;
        prevBodyYaw = abstractClientPlayerEntity.prevBodyYaw;
        headYaw = abstractClientPlayerEntity.headYaw;
        prevHeadYaw = abstractClientPlayerEntity.prevHeadYaw;
        pitch = abstractClientPlayerEntity.getPitch();
        prevPitch = abstractClientPlayerEntity.prevPitch;
        if (renderPlayerEvent.isCanceled()) {
            abstractClientPlayerEntity.setYaw(renderPlayerEvent.getYaw());
            abstractClientPlayerEntity.prevYaw = renderPlayerEvent.getYaw();
            abstractClientPlayerEntity.setBodyYaw(renderPlayerEvent.getYaw());
            abstractClientPlayerEntity.prevBodyYaw = renderPlayerEvent.getYaw();
            abstractClientPlayerEntity.setHeadYaw(renderPlayerEvent.getYaw());
            abstractClientPlayerEntity.prevHeadYaw = renderPlayerEvent.getYaw();
            abstractClientPlayerEntity.setPitch(renderPlayerEvent.getPitch());
            abstractClientPlayerEntity.prevPitch = renderPlayerEvent.getPitch();
        }
    }

    /**
     * @param abstractClientPlayerEntity
     * @param f
     * @param g
     * @param matrixStack
     * @param vertexConsumerProvider
     * @param i
     * @param ci
     */
    @Inject(method = "render(Lnet/minecraft/client/network/" +
            "AbstractClientPlayerEntity;FFLnet/minecraft/client/util/" +
            "math/MatrixStack;Lnet/minecraft/client/render " +
            "/VertexConsumerProvider;I)V", at = @At(value = "TAIL"))
    private void onRenderTail(AbstractClientPlayerEntity abstractClientPlayerEntity,
                              float f, float g, MatrixStack matrixStack,
                              VertexConsumerProvider vertexConsumerProvider,
                              int i, CallbackInfo ci) {
        abstractClientPlayerEntity.setYaw(yaw);
        abstractClientPlayerEntity.prevYaw = prevYaw;
        abstractClientPlayerEntity.setBodyYaw(bodyYaw);
        abstractClientPlayerEntity.prevBodyYaw = prevBodyYaw;
        abstractClientPlayerEntity.setHeadYaw(headYaw);
        abstractClientPlayerEntity.prevHeadYaw = prevHeadYaw;
        abstractClientPlayerEntity.setPitch(pitch);
        abstractClientPlayerEntity.prevPitch = prevPitch;
    }
}
