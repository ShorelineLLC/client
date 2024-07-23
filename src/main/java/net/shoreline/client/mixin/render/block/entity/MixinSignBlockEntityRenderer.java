package net.shoreline.client.mixin.render.block.entity;

import net.minecraft.block.entity.SignText;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.SignBlockEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.shoreline.client.impl.event.render.block.entity.RenderSignTextEvent;
import net.shoreline.eventbus.EventBus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SignBlockEntityRenderer.class)
public class MixinSignBlockEntityRenderer
{
    @Inject(method = "renderText", at = @At(value = "HEAD"), cancellable = true)
    private void hookRenderText(BlockPos pos, SignText signText, MatrixStack matrices, VertexConsumerProvider vertexConsumers,
                                int light, int lineHeight, int lineWidth, boolean front, CallbackInfo ci)
    {
        RenderSignTextEvent renderSignTextEvent = new RenderSignTextEvent();
        EventBus.INSTANCE.dispatch(renderSignTextEvent);
        if (renderSignTextEvent.isCanceled())
        {
            ci.cancel();
        }
    }
}
