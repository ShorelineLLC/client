package net.shoreline.client.mixin.gui.hud;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameOverlayRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.shoreline.client.impl.event.gui.hud.OverlayEvent;
import net.shoreline.eventbus.EventBus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameOverlayRenderer.class)
public class MixinInGameOverlayRenderer
{
    @Inject(method = "renderFireOverlay", at = @At(value = "HEAD"), cancellable = true)
    private static void hookRenderFireOverlay(MatrixStack matrices,
                                              VertexConsumerProvider vertexConsumers,
                                              CallbackInfo ci)
    {
        OverlayEvent.Fire renderOverlayEvent = new OverlayEvent.Fire();
        EventBus.INSTANCE.dispatch(renderOverlayEvent);
        if (renderOverlayEvent.isCanceled())
        {
            ci.cancel();
        }
    }

    @Inject(method = "renderUnderwaterOverlay", at = @At(value = "HEAD"), cancellable = true)
    private static void hookRenderUnderwaterOverlay(MinecraftClient client,
                                                    MatrixStack matrices,
                                                    VertexConsumerProvider vertexConsumers,
                                                    CallbackInfo ci)
    {
        OverlayEvent.Water renderOverlayEvent = new OverlayEvent.Water();
        EventBus.INSTANCE.dispatch(renderOverlayEvent);
        if (renderOverlayEvent.isCanceled())
        {
            ci.cancel();
        }
    }

    @Inject(method = "renderInWallOverlay", at = @At(value = "HEAD"), cancellable = true)
    private static void hookRenderFireOverlay(Sprite sprite,
                                              MatrixStack matrices,
                                              VertexConsumerProvider vertexConsumers,
                                              CallbackInfo ci)
    {
        OverlayEvent.Blocks renderOverlayEvent = new OverlayEvent.Blocks();
        EventBus.INSTANCE.dispatch(renderOverlayEvent);
        if (renderOverlayEvent.isCanceled())
        {
            ci.cancel();
        }
    }
}
