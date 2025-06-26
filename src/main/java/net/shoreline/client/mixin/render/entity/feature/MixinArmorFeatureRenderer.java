package net.shoreline.client.mixin.render.entity.feature;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.ArmorFeatureRenderer;
import net.minecraft.client.render.entity.state.BipedEntityRenderState;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.shoreline.client.impl.event.render.entity.feature.RenderArmorEvent;
import net.shoreline.eventbus.EventBus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ArmorFeatureRenderer.class)
public class MixinArmorFeatureRenderer
{
    @Inject(method = "render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/client/render/entity/state/BipedEntityRenderState;FF)V",
            at = @At(value = "HEAD"),
            cancellable = true)
    private void hookRenderArmor(MatrixStack matrixStack,
                                 VertexConsumerProvider vertexConsumerProvider,
                                 int i,
                                 BipedEntityRenderState bipedEntityRenderState,
                                 float f,
                                 float g,
                                 CallbackInfo ci)
    {
        if (bipedEntityRenderState instanceof PlayerEntityRenderState)
        {
            RenderArmorEvent renderArmorEvent = new RenderArmorEvent();
            EventBus.INSTANCE.dispatch(renderArmorEvent);
            if (renderArmorEvent.isCanceled())
            {
                ci.cancel();
            }
        }
    }
}
