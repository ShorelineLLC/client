package net.shoreline.client.mixin.render;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.render.*;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.ObjectAllocator;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.util.math.RotationAxis;
import net.shoreline.client.impl.event.particle.ParticleEvent;
import net.shoreline.client.impl.event.render.RenderEntityWorldEvent;
import net.shoreline.client.impl.event.render.RenderPlayerThirdPersonEvent;
import net.shoreline.client.impl.event.render.RenderShaderEvent;
import net.shoreline.client.impl.event.render.RenderWorldEvent;
import net.shoreline.client.impl.imixin.IWorldRenderer;
import net.shoreline.eventbus.EventBus;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WorldRenderer.class)
public abstract class MixinWorldRenderer implements IWorldRenderer
{
    @Override
    @Accessor(value = "frustum")
    public abstract Frustum getFrustum();

    @Inject(method = "render", at = @At(value = "RETURN"))
    private void hookRenderWorld(ObjectAllocator allocator,
                                 RenderTickCounter tickCounter,
                                 boolean renderBlockOutline,
                                 Camera camera,
                                 GameRenderer gameRenderer,
                                 Matrix4f positionMatrix,
                                 Matrix4f projectionMatrix,
                                 CallbackInfo ci)
    {
        MatrixStack matrixStack = new MatrixStack();
        matrixStack.push();
        matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(gameRenderer.getCamera().getPitch()));
        matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(gameRenderer.getCamera().getYaw() + 180f));

        float tickDelta = tickCounter.getTickDelta(true);
        RenderWorldEvent.Post renderWorldEvent = new RenderWorldEvent.Post(matrixStack, tickDelta);
        EventBus.INSTANCE.dispatch(renderWorldEvent);

        matrixStack.pop();
    }

    @Inject(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/util/profiler/Profiler;swap(Ljava/lang/String;)V",
                    ordinal = 0,
                    shift = At.Shift.BEFORE))
    private void hookRenderSwap(ObjectAllocator allocator,
                                RenderTickCounter tickCounter,
                                boolean renderBlockOutline,
                                Camera camera,
                                GameRenderer gameRenderer,
                                Matrix4f positionMatrix,
                                Matrix4f projectionMatrix,
                                CallbackInfo info)
    {
        RenderShaderEvent renderShaderEvent = new RenderShaderEvent();
        EventBus.INSTANCE.dispatch(renderShaderEvent);
    }

    @WrapOperation(method = "renderEntity", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/render/entity/EntityRenderDispatcher;render(Lnet/minecraft/entity/Entity;DDDFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V"))
    private <E extends Entity> void hookRenderEntity(EntityRenderDispatcher instance,
                                                     E entity,
                                                     double x,
                                                     double y,
                                                     double z,
                                                     float tickDelta,
                                                     MatrixStack matrices,
                                                     VertexConsumerProvider vertexConsumers,
                                                     int light,
                                                     Operation<Void> original)
    {
        RenderEntityWorldEvent event = new RenderEntityWorldEvent(entity, vertexConsumers);
        EventBus.INSTANCE.dispatch(event);
        if (event.isCanceled() && event.getVertexConsumerProvider() == null)
        {
            return;
        }

        original.call(instance, entity, x, y, z, tickDelta, matrices, event.isCanceled() ? event.getVertexConsumerProvider() : vertexConsumers, light);
    }

    @Redirect(method = "getEntitiesToRender", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/render/Camera;isThirdPerson()Z"))
    public boolean hookGetEntitiesToRender(Camera instance)
    {
        RenderPlayerThirdPersonEvent renderPlayerEvent = new RenderPlayerThirdPersonEvent();
        EventBus.INSTANCE.dispatch(renderPlayerEvent);
        return renderPlayerEvent.isCanceled() || instance.isThirdPerson();
    }

    @Inject(method = "spawnParticle(Lnet/minecraft/particle/ParticleEffect;ZZDDDDDD)Lnet/minecraft/client/particle/Particle;", at = @At(value = "HEAD"), cancellable = true)
    private void hookAddParticle(ParticleEffect parameters,
                                 boolean force,
                                 boolean canSpawnOnMinimal,
                                 double x,
                                 double y,
                                 double z,
                                 double velocityX,
                                 double velocityY,
                                 double velocityZ,
                                 CallbackInfoReturnable<Particle> cir)
    {
        ParticleEvent particleEvent = new ParticleEvent(parameters.getType());
        EventBus.INSTANCE.dispatch(particleEvent);
        if (particleEvent.isCanceled())
        {
            cir.cancel();
            cir.setReturnValue(null);
        }
    }

    @Inject(method = "onResized", at = @At("TAIL"))
    private void hookResized(int width, int height, CallbackInfo info)
    {
        RenderWorldEvent.Resized resizedEvent = new RenderWorldEvent.Resized(width, height);
        EventBus.INSTANCE.dispatch(resizedEvent);
    }
}
