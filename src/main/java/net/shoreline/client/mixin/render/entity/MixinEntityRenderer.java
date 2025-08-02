package net.shoreline.client.mixin.render.entity;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.text.Text;
import net.shoreline.client.impl.event.render.entity.RenderEntityLabelEvent;
import net.shoreline.eventbus.EventBus;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public class MixinEntityRenderer
{
    @Shadow
    @Final
    protected EntityRenderDispatcher dispatcher;

    @Inject(method = "renderLabelIfPresent", at = @At(value = "HEAD"), cancellable = true)
    public void hookRenderLabelIfPresent(EntityRenderState state,
                                         Text text,
                                         MatrixStack matrices,
                                         VertexConsumerProvider vertexConsumers,
                                         int light,
                                         CallbackInfo ci)
    {
        RenderEntityLabelEvent renderLabelEvent = new RenderEntityLabelEvent(dispatcher.targetedEntity);
        EventBus.INSTANCE.dispatch(renderLabelEvent);
        if (renderLabelEvent.isCanceled())
        {
            ci.cancel();
        }
    }
}
