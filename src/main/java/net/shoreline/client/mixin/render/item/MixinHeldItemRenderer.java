package net.shoreline.client.mixin.render.item;

import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.shoreline.client.impl.event.render.item.RenderHeldItemEvent;
import net.shoreline.eventbus.EventBus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HeldItemRenderer.class)
public class MixinHeldItemRenderer
{
    @Inject(method = "renderFirstPersonItem", at = @At(value = "HEAD"), cancellable = true)
    private void hookRenderFirstPersonItem(AbstractClientPlayerEntity player,
                                           float tickDelta,
                                           float pitch,
                                           Hand hand,
                                           float swingProgress,
                                           ItemStack item,
                                           float equipProgress,
                                           MatrixStack matrices,
                                           VertexConsumerProvider vertexConsumers,
                                           int light,
                                           CallbackInfo ci)
    {
        RenderHeldItemEvent.Pre renderFirstPersonEvent = new RenderHeldItemEvent.Pre();
        EventBus.INSTANCE.dispatch(renderFirstPersonEvent);
        if (renderFirstPersonEvent.isCanceled())
        {
            ci.cancel();
        }
    }
}
