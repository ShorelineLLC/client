package net.shoreline.client.mixin.render.entity;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.FireworkRocketEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.shoreline.client.impl.event.render.entity.RenderFireworkRocketEvent;
import net.shoreline.eventbus.EventBus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author linus
 * @since 1.0
 */
@Mixin(FireworkRocketEntityRenderer.class)
public class MixinFireworkRocketEntityRenderer
{
    /**
     * @param fireworkRocketEntity
     * @param f
     * @param g
     * @param matrixStack
     * @param vertexConsumerProvider
     * @param i
     * @param ci
     */
    @Inject(method = "render(Lnet/minecraft/entity/projectile/" +
            "FireworkRocketEntity;FFLnet/minecraft/client/util/math/ " +
            "MatrixStack;Lnet/minecraft/client/render/ " +
            "VertexConsumerProvider;I)V", at = @At(value = "HEAD"),
            cancellable = true)
    private void hookRender(FireworkRocketEntity fireworkRocketEntity,
                            float f, float g, MatrixStack matrixStack,
                            VertexConsumerProvider vertexConsumerProvider,
                            int i, CallbackInfo ci)
    {
        RenderFireworkRocketEvent renderFireworkRocketEvent =
                new RenderFireworkRocketEvent();
        EventBus.INSTANCE.dispatch(renderFireworkRocketEvent);
        if (renderFireworkRocketEvent.isCanceled())
        {
            ci.cancel();
        }
    }
}
