package net.shoreline.client.mixin.render;

import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.shoreline.client.api.render.layers.RenderLayersClient;
import net.shoreline.client.impl.event.PerspectiveEvent;
import net.shoreline.client.impl.event.render.RenderShaderEvent;
import net.shoreline.client.impl.event.render.RenderWorldBorderEvent;
import net.shoreline.client.impl.event.render.RenderWorldEvent;
import net.shoreline.client.util.Globals;
import net.shoreline.eventbus.EventBus;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author linus
 * @since 1.0
 */
@Mixin(WorldRenderer.class)
public class MixinWorldRenderer implements Globals
{

    /**
     * @param matrices
     * @param tickDelta
     * @param limitTime
     * @param renderBlockOutline
     * @param camera
     * @param gameRenderer
     * @param lightmapTextureManager
     * @param positionMatrix
     * @param ci
     */
    @Inject(method = "render", at = @At(value = "RETURN"))
    private void hookRender(MatrixStack matrices, float tickDelta,
                            long limitTime, boolean renderBlockOutline,
                            Camera camera, GameRenderer gameRenderer,
                            LightmapTextureManager lightmapTextureManager,
                            Matrix4f positionMatrix, CallbackInfo ci)
    {
        final RenderWorldEvent renderWorldEvent =
                new RenderWorldEvent(matrices, tickDelta);
        EventBus.INSTANCE.dispatch(renderWorldEvent);
    }

    /**
     * @param camera
     * @param ci
     */
    @Inject(method = "renderWorldBorder", at = @At(value = "HEAD"), cancellable = true)
    private void hookRenderWorldBorder(Camera camera, CallbackInfo ci)
    {
        RenderWorldBorderEvent renderWorldBorderEvent =
                new RenderWorldBorderEvent();
        EventBus.INSTANCE.dispatch(renderWorldBorderEvent);
        if (renderWorldBorderEvent.isCanceled())
        {
            ci.cancel();
        }
    }

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Camera;isThirdPerson()Z"))
    public boolean hookRender(Camera instance)
    {
        PerspectiveEvent perspectiveEvent = new PerspectiveEvent(instance);
        EventBus.INSTANCE.dispatch(perspectiveEvent);

        if (perspectiveEvent.isCanceled())
        {
            return true;
        }
        return instance.isThirdPerson();
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/OutlineVertexConsumerProvider;draw()V", shift = At.Shift.AFTER))
    private void hookRender$1(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera,
                              GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f projectionMatrix, CallbackInfo ci)
    {
        RenderShaderEvent renderOutlineShaderEvent = new RenderShaderEvent(matrices, tickDelta);
        EventBus.INSTANCE.dispatch(renderOutlineShaderEvent);
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiler/Profiler;swap(Ljava/lang/String;)V", ordinal = 12))
    private void hookRender$2(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera,
                              GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f projectionMatrix, CallbackInfo ci)
    {
        RenderShaderEvent.BlockEntities renderOutlineShaderEvent = new RenderShaderEvent.BlockEntities(matrices, tickDelta);
        EventBus.INSTANCE.dispatch(renderOutlineShaderEvent);
    }

//    /**
//     *
//     * @param builder
//     * @param f
//     * @param cir
//     */
//    @Inject(method = "renderSky(Lnet/minecraft/client/render/BufferBuilder;F)" +
//            "Lnet/minecraft/client/render/BufferBuilder$BuiltBuffer;",
//            at = @At(value = "HEAD"), cancellable = true)
//    private static void hookRenderSky(BufferBuilder builder, float f,
//                                      CallbackInfoReturnable<BufferBuilder.BuiltBuffer> cir) {
//        cir.cancel();
//        float g = Math.signum(f) * 512.0f;
//        RenderSystem.setShader(GameRenderer::getPositionProgram);
//        builder.begin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION);
//        builder.vertex(0.0, -64.0f, 0.0).next();
//        for (int i = -180; i <= 180; i += 45) {
//            builder.vertex(g * MathHelper.cos((float)i * ((float)Math.PI / 180)), -64.0f, 512.0f * MathHelper.sin((float)i * ((float)Math.PI / 180))).next();
//        }
//        cir.setReturnValue(builder.end());
//    }
}