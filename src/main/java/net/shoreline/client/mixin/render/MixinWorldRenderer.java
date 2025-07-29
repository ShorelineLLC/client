package net.shoreline.client.mixin.render;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.FramePass;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.ObjectAllocator;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;
import net.shoreline.client.impl.event.render.RenderPlayerThirdPersonEvent;
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

    @Inject(method = "render", at = @At(value = "RETURN"))
    private void hookRenderWorld(ObjectAllocator allocator,
                                 RenderTickCounter tickCounter,
                                 boolean renderBlockOutline,
                                 Camera camera,
                                 Matrix4f positionMatrix,
                                 Matrix4f projectionMatrix,
                                 GpuBufferSlice fog,
                                 Vector4f fogColor,
                                 boolean shouldRenderSky,
                                 CallbackInfo ci)
    {
        MatrixStack matrixStack = new MatrixStack();
        matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(client.gameRenderer.getCamera().getPitch()));
        matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(client.gameRenderer.getCamera().getYaw() + 180f));

        RenderWorldEvent.Post renderWorldEvent = new RenderWorldEvent.Post(matrixStack, tickCounter.getTickProgress(true));
        EventBus.INSTANCE.dispatch(renderWorldEvent);
    }

    @Redirect(method = "getEntitiesToRender", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/render/Camera;isThirdPerson()Z"))
    public boolean hookGetEntitiesToRender(Camera instance)
    {
        RenderPlayerThirdPersonEvent renderPlayerEvent = new RenderPlayerThirdPersonEvent();
        EventBus.INSTANCE.dispatch(renderPlayerEvent);
        return renderPlayerEvent.isCanceled() || instance.isThirdPerson();
    }
}
