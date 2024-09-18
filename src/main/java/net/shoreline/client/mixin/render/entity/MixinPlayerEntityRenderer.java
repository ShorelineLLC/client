package net.shoreline.client.mixin.render.entity;

import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.shoreline.client.impl.event.render.entity.ElytraTransformEvent;
import net.shoreline.client.impl.event.render.entity.RenderPlayerEvent;
import net.shoreline.eventbus.EventBus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author linus
 * @see PlayerEntityRenderer
 * @since 1.0
 */
@Mixin(PlayerEntityRenderer.class)
public class MixinPlayerEntityRenderer
{
    //
    @Unique
    private float yaw, prevYaw, bodyYaw, prevBodyYaw, headYaw, prevHeadYaw;
    @Unique
    private float pitch, prevPitch;
    @Unique
    private boolean prevSneaking;
    @Unique
    private boolean prevFallFlying;

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
                              int i, CallbackInfo ci)
    {
        final RenderPlayerEvent renderPlayerEvent =
                new RenderPlayerEvent(abstractClientPlayerEntity);
        EventBus.INSTANCE.dispatch(renderPlayerEvent);
        yaw = abstractClientPlayerEntity.getYaw();
        prevYaw = abstractClientPlayerEntity.prevYaw;
        bodyYaw = abstractClientPlayerEntity.bodyYaw;
        prevBodyYaw = abstractClientPlayerEntity.prevBodyYaw;
        headYaw = abstractClientPlayerEntity.headYaw;
        prevHeadYaw = abstractClientPlayerEntity.prevHeadYaw;
        pitch = abstractClientPlayerEntity.getPitch();
        prevPitch = abstractClientPlayerEntity.prevPitch;
        prevSneaking = abstractClientPlayerEntity.isSneaking();
        if (renderPlayerEvent.isCanceled())
        {
            abstractClientPlayerEntity.setYaw(renderPlayerEvent.getYaw());
            abstractClientPlayerEntity.prevYaw = renderPlayerEvent.getYaw();
            abstractClientPlayerEntity.setBodyYaw(renderPlayerEvent.getYaw());
            abstractClientPlayerEntity.prevBodyYaw = renderPlayerEvent.getYaw();
            abstractClientPlayerEntity.setHeadYaw(renderPlayerEvent.getYaw());
            abstractClientPlayerEntity.prevHeadYaw = renderPlayerEvent.getYaw();
            abstractClientPlayerEntity.setPitch(renderPlayerEvent.getPitch());
            abstractClientPlayerEntity.prevPitch = renderPlayerEvent.getPitch();
            // abstractClientPlayerEntity.setSneaking(renderPlayerEvent.isSneaking());
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
                              int i, CallbackInfo ci)
    {
        abstractClientPlayerEntity.setYaw(yaw);
        abstractClientPlayerEntity.prevYaw = prevYaw;
        abstractClientPlayerEntity.setBodyYaw(bodyYaw);
        abstractClientPlayerEntity.prevBodyYaw = prevBodyYaw;
        abstractClientPlayerEntity.setHeadYaw(headYaw);
        abstractClientPlayerEntity.prevHeadYaw = prevHeadYaw;
        abstractClientPlayerEntity.setPitch(pitch);
        abstractClientPlayerEntity.prevPitch = prevPitch;
        // abstractClientPlayerEntity.setSneaking(prevSneaking);
    }

    @Redirect(method = "setupTransforms(Lnet/minecraft/client/network/AbstractClientPlayerEntity;Lnet/minecraft/client/util/math/MatrixStack;FFFF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/AbstractClientPlayerEntity;isFallFlying()Z"))
    private boolean hookSetupTransforms(AbstractClientPlayerEntity instance)
    {
        ElytraTransformEvent elytraTransformEvent = new ElytraTransformEvent(instance);
        EventBus.INSTANCE.dispatch(elytraTransformEvent);
        if (elytraTransformEvent.isCanceled())
        {
            return false;
        }
        return instance.isFallFlying();
    }
}
