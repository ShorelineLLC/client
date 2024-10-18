package net.shoreline.client.mixin.render.entity;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.shoreline.client.impl.event.render.entity.RenderEntityEvent;
import net.shoreline.client.impl.event.render.entity.RenderEntityInvisibleEvent;
import net.shoreline.client.impl.event.render.entity.RenderThroughWallsEvent;
import net.shoreline.eventbus.EventBus;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(LivingEntityRenderer.class)
public abstract class MixinLivingEntityRenderer<T extends LivingEntity, M extends EntityModel<T>>
{
    //
    @Shadow
    protected M model;
    //
    @Shadow
    @Final
    protected List<FeatureRenderer<T, M>> features;

    @Shadow
    protected abstract RenderLayer getRenderLayer(T entity, boolean showBody, boolean translucent, boolean showOutline);

    /**
     * @param livingEntity
     * @param f
     * @param g
     * @param matrixStack
     * @param vertexConsumerProvider
     * @param i
     * @param ci
     */
    @Inject(method = "render*", at = @At(value = "HEAD"), cancellable = true)
    private void hookRender(LivingEntity livingEntity, float f, float g,
                            MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci)
    {
        RenderEntityEvent renderEntityEvent = new RenderEntityEvent((LivingEntityRenderer) (Object) this, livingEntity,
                f, g, matrixStack, vertexConsumerProvider, i, model, getRenderLayer((T) livingEntity, false, false, false), features);
        EventBus.INSTANCE.dispatch(renderEntityEvent);
        if (renderEntityEvent.isCanceled())
        {
            ci.cancel();
        }
    }

    @Redirect(method = "render*", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;isInvisibleTo(Lnet/minecraft/entity/player/PlayerEntity;)Z"))
    private boolean redirectRender$isInvisibleTo(LivingEntity entity, PlayerEntity player)
    {
        final RenderEntityInvisibleEvent event = new RenderEntityInvisibleEvent(entity);
        EventBus.INSTANCE.dispatch(event);
        if (event.isCanceled())
        {
            return false;
        }
        return entity.isInvisibleTo(player);
    }


    @Inject(method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At("HEAD"), cancellable = true)
    private void hookRender$1(T livingEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci)
    {
        RenderThroughWallsEvent renderThroughWallsEvent = new RenderThroughWallsEvent(livingEntity);
        EventBus.INSTANCE.dispatch(renderThroughWallsEvent);
        if (renderThroughWallsEvent.isCanceled())
        {
            RenderSystem.disableDepthTest();
        }
    }

    @Inject(method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At("TAIL"))
    private void hookRender$2(T livingEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci)
    {
        RenderThroughWallsEvent renderThroughWallsEvent = new RenderThroughWallsEvent(livingEntity);
        EventBus.INSTANCE.dispatch(renderThroughWallsEvent);
        if (renderThroughWallsEvent.isCanceled())
        {
            RenderSystem.enableDepthTest();
        }
    }
}
