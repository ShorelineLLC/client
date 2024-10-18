package net.shoreline.client.mixin.render.entity;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.ItemEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.ItemEntity;
import net.shoreline.client.impl.event.render.entity.RenderItemEvent;
import net.shoreline.eventbus.EventBus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author linus
 * @since 1.0
 */
@Mixin(ItemEntityRenderer.class)
public class MixinItemEntityRenderer
{
    /**
     * @param itemEntity
     * @param f
     * @param g
     * @param matrixStack
     * @param vertexConsumerProvider
     * @param i
     * @param ci
     */
    @Inject(method = "render(Lnet/minecraft/entity/ItemEntity;" +
            "FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/" +
            "client/render/VertexConsumerProvider;I)V", at = @At(value = "HEAD"),
            cancellable = true)
    private void hookRender(ItemEntity itemEntity, float f, float g,
                            MatrixStack matrixStack,
                            VertexConsumerProvider vertexConsumerProvider,
                            int i, CallbackInfo ci)
    {
        RenderItemEvent renderItemEvent = new RenderItemEvent(itemEntity);
        EventBus.INSTANCE.dispatch(renderItemEvent);
        if (renderItemEvent.isCanceled())
        {
            ci.cancel();
        }
    }
}
