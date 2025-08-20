package net.shoreline.client.mixin.render;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.ObjectAllocator;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.RotationAxis;
import net.shoreline.client.impl.Managers;
import net.shoreline.client.impl.event.render.RenderEntityWorldEvent;
import net.shoreline.client.impl.event.render.RenderPlayerThirdPersonEvent;
import net.shoreline.client.impl.event.render.RenderShaderEvent;
import net.shoreline.client.impl.event.render.RenderWorldEvent;
import net.shoreline.eventbus.EventBus;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public class MixinWorldRenderer
{
    @Shadow
    @Final
    private MinecraftClient client;

    @Shadow
    @Final
    private EntityRenderDispatcher entityRenderDispatcher;

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
        matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(client.gameRenderer.getCamera().getPitch()));
        matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(client.gameRenderer.getCamera().getYaw() + 180f));

        RenderWorldEvent.Post renderWorldEvent = new RenderWorldEvent.Post(matrixStack, tickCounter.getTickDelta(true));
        EventBus.INSTANCE.dispatch(renderWorldEvent);
    }

    @Inject(method = "render", at = @At(value = "INVOKE",
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
    private void hookRenderEntity(EntityRenderDispatcher instance,
                                  Entity entity,
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

    @Inject(method = "onResized", at = @At("TAIL"))
    private void hookResized(int width, int height, CallbackInfo info)
    {
        if (Managers.SHADER != null)
        {
            Managers.SHADER.resize(width, height);
        }
    }
}
