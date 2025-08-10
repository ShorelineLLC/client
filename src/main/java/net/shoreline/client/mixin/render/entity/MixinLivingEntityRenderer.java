package net.shoreline.client.mixin.render.entity;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import net.shoreline.client.impl.imixin.IModel;
import net.shoreline.client.impl.module.client.ThemeModule;
import net.shoreline.client.impl.module.render.ChamsModule;
import net.shoreline.client.impl.render.ColorUtil;
import net.shoreline.client.impl.render.Layers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import java.awt.*;

@Mixin(LivingEntityRenderer.class)
public abstract class MixinLivingEntityRenderer<T extends LivingEntity,
        S extends LivingEntityRenderState,
        M extends EntityModel<? super S>>
{
    @Shadow
    public abstract Identifier getTexture(S state);

    @Shadow
    protected M model;

    @Unique
    protected LivingEntity last;

    @Inject(
            method = "updateRenderState(Lnet/minecraft/entity/LivingEntity;" +
                    "Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;F)V",
            at = @At(value = "HEAD"))
    private void updateRenderStateHook(T livingEntity, S livingEntityRenderState, float f, CallbackInfo info)
    {
        last = livingEntity;
    }

    @ModifyReturnValue(method = "getRenderLayer", at = @At(value = "RETURN"))
    private RenderLayer getRenderLayerHook(RenderLayer original, @Local(argsOnly = true) S state, @Local(ordinal = 2, argsOnly = true) boolean showOutline)
    {
        Identifier identifier = this.getTexture(state);
        if (ChamsModule.getInstance().isEnabled() && ChamsModule.getInstance().xqz.getValue() && ChamsModule.getInstance().isValid(last))
        {
            return Layers.ENTITY.apply(identifier, showOutline);
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
        if (ChamsModule.getInstance().isEnabled() && ChamsModule.getInstance().xqz.getValue() && ChamsModule.getInstance().isValid(last))
        {
            int alpha = (int) (ChamsModule.getInstance().opacity.getValue() * 255.0f);
            alpha = Math.max(0, Math.min(alpha, 255));
            args.set(4, new Color(255, 255, 255, alpha).getRGB());
        }
    }

    @Inject(
            method = "render(Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;" +
                    "Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/render/entity/model/EntityModel;setAngles(Lnet/minecraft/client/render/entity/state/EntityRenderState;)V",
                    shift = At.Shift.AFTER))
    private void setAnglesHook(S livingEntityRenderState, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo info)
    {
        boolean valid = ChamsModule.getInstance().isValid(last);
        ((IModel) model).cancelModel(valid);
        if (ChamsModule.getInstance().isEnabled() && valid)
        {
            if (ChamsModule.getInstance().shine.getValue())
            {
                Layers.QUADS_GLINT.startDrawing();
                VertexConsumer consumer = ItemRenderer.getArmorGlintConsumer(vertexConsumerProvider, Layers.QUADS_GLINT, true);
                model.render(matrixStack, consumer, i, OverlayTexture.DEFAULT_UV, ColorUtil.withTransparency(ThemeModule.INSTANCE.getPrimaryColor(), 1.0f));
                Layers.QUADS_GLINT.endDrawing();
            }
        }
    }
}
