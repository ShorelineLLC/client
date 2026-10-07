package net.shoreline.client.mixin.render.entity;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.item.ItemModelManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import net.shoreline.client.impl.event.entity.EntityHurtEvent;
import net.shoreline.client.impl.imixin.IModel;
import net.shoreline.client.impl.module.render.ChamsModule;
import net.shoreline.client.impl.render.ChamsRenderer;
import net.shoreline.client.impl.render.Layers;
import net.shoreline.eventbus.EventBus;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import java.awt.*;
import java.util.Collections;
import java.util.List;

@Mixin(LivingEntityRenderer.class)
public abstract class MixinLivingEntityRenderer<T extends LivingEntity,
        S extends LivingEntityRenderState,
        M extends EntityModel<? super S>>
    extends MixinEntityRenderer<T, S>
{
    @Shadow
    public abstract Identifier getTexture(S state);

    @Shadow
    protected M model;

    @Shadow
    protected abstract void scale(S state, MatrixStack matrices);

    @Shadow
    protected abstract boolean isVisible(S state);

    @Shadow
    private static float clampBodyYaw(LivingEntity entity, float degrees, float tickDelta) {return 0;}

    @Shadow
    public static boolean shouldFlipUpsideDown(LivingEntity entity) {return false;}

    @Shadow @Final protected ItemModelManager itemModelResolver;
    @Unique
    protected LivingEntity last;

    @ModifyExpressionValue(
            method = "render(Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;" +
                    "Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/client/render/entity/LivingEntityRenderer;features:Ljava/util/List;"))
    private List<FeatureRenderer<S, M>> featuresHook(List<FeatureRenderer<S, M>> original)
    {
        ChamsModule.ChamsMode mode = ChamsModule.getInstance().mode.getValue();
        if (ChamsModule.getInstance().isEnabled()
                && (mode == ChamsModule.ChamsMode.X_Q_Z
                    || mode == ChamsModule.ChamsMode.SHINE
                    || ChamsRenderer.rendering))
        {
            return Collections.emptyList();
        }

        return original;
    }

    @Inject(
            method = "updateRenderState(Lnet/minecraft/entity/LivingEntity;" +
                    "Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;F)V",
            at = @At(value = "HEAD"))
    private void updateRenderStateHook(T livingEntity, S livingEntityRenderState, float f, CallbackInfo info)
    {
        last = livingEntity;
    }

    @Inject(
            method = "updateRenderState(Lnet/minecraft/entity/LivingEntity;" +
                    "Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;F)V",
            at = @At(value = "RETURN"))
    private void updateRenderStateHook_Post(T livingEntity, S livingEntityRenderState, float f, CallbackInfo ci)
    {
        if (livingEntity instanceof ChamsModule.PopEntity)
        {
            livingEntityRenderState.limbFrequency = livingEntity.limbAnimator.getPos(1f);
            livingEntityRenderState.limbAmplitudeMultiplier = livingEntity.limbAnimator.getSpeed(1f);
        }
    }

    @ModifyReturnValue(method = "getRenderLayer", at = @At(value = "RETURN"))
    private RenderLayer getRenderLayerHook(RenderLayer original, @Local(argsOnly = true) S state, @Local(ordinal = 2, argsOnly = true) boolean showOutline)
    {
        Identifier identifier = this.getTexture(state);
        if (ChamsModule.getInstance().isEnabled() && ChamsModule.getInstance().isValid(last))
        {
            return Layers.ENTITY.apply(identifier, true);
        }

        return original;
    }

    @ModifyArgs(method = "render(Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;" +
            "Lnet/minecraft/client/util/math/MatrixStack;" +
            "Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/render/entity/model/EntityModel;" +
                            "render(Lnet/minecraft/client/util/math/MatrixStack;" +
                            "Lnet/minecraft/client/render/VertexConsumer;III)V"))
    private void renderHook(Args args, S livingEntityRenderState, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i)
    {
        if (ChamsModule.getInstance().isEnabled())
        {
            float opacity = ChamsModule.getInstance().getOpacity();
            if (opacity != 1.0f && ChamsModule.getInstance().isValid(last))
            {
                int alpha = (int) (ChamsModule.getInstance().getOpacity() * 255.0f);
                alpha = Math.max(0, Math.min(alpha, 255));
                args.set(4, new Color(255, 255, 255, alpha).getRGB());
            }
        }
    }

    @Inject(
            method = "render(Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;" +
                    "Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/render/entity/model/EntityModel;" +
                            "render(Lnet/minecraft/client/util/math/MatrixStack;" +
                            "Lnet/minecraft/client/render/VertexConsumer;III)V",
                    shift = At.Shift.AFTER))
    private void setAnglesHook(S livingEntityRenderState, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo info)
    {
        if (ChamsRenderer.rendering)
        {
            return;
        }

        boolean valid = ChamsModule.getInstance().isValid(last);
        ((IModel) model).cancelModel(valid);
        if (ChamsModule.getInstance().isEnabled() && valid)
        {
            if (model instanceof PlayerEntityModel playerEntityModel)
            {
                boolean extraLayer = ChamsModule.getInstance().extraLayer.getValue();
                playerEntityModel.leftPants.visible = extraLayer;
                playerEntityModel.rightPants.visible = extraLayer;
                playerEntityModel.leftSleeve.visible = extraLayer;
                playerEntityModel.rightSleeve.visible = extraLayer;
                playerEntityModel.jacket.visible = extraLayer;
                playerEntityModel.hat.visible = extraLayer;
            }

            int color = ChamsModule.getInstance().getColor(last).getRGB();
            if (ChamsModule.getInstance().mode.getValue() == ChamsModule.ChamsMode.SHINE)
            {
                Layers.QUADS_GLINT.startDrawing();
                VertexConsumerProvider.Immediate provider = MinecraftClient.getInstance().getBufferBuilders().getEffectVertexConsumers();
                VertexConsumer consumer = ItemRenderer.getArmorGlintConsumer(provider, Layers.QUADS_GLINT, true);
                this.model.render(matrixStack, consumer, i, OverlayTexture.DEFAULT_UV, color);
                Layers.QUADS_GLINT.endDrawing();
            }

            ChamsRenderer renderer = ChamsModule.getInstance().mode.getValue().getRenderer();
            float tickDelta = MinecraftClient.getInstance().getRenderTickCounter().getTickDelta(false);
            ChamsRenderer.render(renderer, last, tickDelta, color);
        }
    }

    @Redirect(
            method = "getOverlay",
            at = @At(value = "FIELD",
                    target = "Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;hurt:Z"))
    private static boolean hurtHook(LivingEntityRenderState instance)
    {
        EntityHurtEvent event = new EntityHurtEvent();
        EventBus.INSTANCE.dispatch(event);
        if (event.isCanceled())
        {
            return false;
        }

        return instance.hurt;
    }
}
