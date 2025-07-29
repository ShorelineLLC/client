package net.shoreline.client.mixin.render;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;
import net.shoreline.client.impl.event.render.RenderPlayerThirdPersonEvent;
import net.shoreline.client.impl.event.render.RenderWorldEvent;
import net.shoreline.eventbus.EventBus;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(WorldRenderer.class)
public class MixinWorldRenderer
{
    @Shadow
    @Final
    private BufferBuilderStorage bufferBuilders;

    @Redirect(method = "renderMain", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/render/FramePass;setRenderer(Ljava/lang/Runnable;)V"))
    public void hookSetRenderer(FramePass instance,
                                Runnable runnable,
                                @Local(argsOnly = true) Camera camera)
    {
        VertexConsumerProvider.Immediate immediate = bufferBuilders.getEntityVertexConsumers();
        instance.setRenderer(() ->
        {
            runnable.run();

            MatrixStack matrixStack = new MatrixStack();
            Vec3d cameraPos = camera.getPos();
            matrixStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);

            RenderWorldEvent.Post renderWorldEvent = new RenderWorldEvent.Post(matrixStack, immediate);
            EventBus.INSTANCE.dispatch(renderWorldEvent);
            immediate.draw();
        });
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
