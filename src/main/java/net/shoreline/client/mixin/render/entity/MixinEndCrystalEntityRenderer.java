package net.shoreline.client.mixin.render.entity;

import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EndCrystalEntityRenderer;
import net.minecraft.client.render.entity.model.EndCrystalEntityModel;
import net.minecraft.client.render.entity.state.EndCrystalEntityRenderState;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.util.Identifier;
import net.shoreline.client.impl.imixin.IModel;
import net.shoreline.client.impl.module.client.ThemeModule;
import net.shoreline.client.impl.module.render.ChamsModule;
import net.shoreline.client.impl.render.ColorUtil;
import net.shoreline.client.impl.render.Layers;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EndCrystalEntityRenderer.class)
public class MixinEndCrystalEntityRenderer
{
    @Mutable
    @Shadow
    @Final
    private static RenderLayer END_CRYSTAL;

    @Shadow
    @Final
    private static Identifier TEXTURE;

    @Shadow
    @Final
    private EndCrystalEntityModel model;

    @Unique
    private EndCrystalEntity last;

    @Unique
    private static final Identifier BLANK = Identifier.of("shoreline", "textures/blank.png");

    @Inject(
            method = "updateRenderState(Lnet/minecraft/entity/decoration/EndCrystalEntity;" +
                    "Lnet/minecraft/client/render/entity/state/EndCrystalEntityRenderState;F)V",
            at = @At(value = "HEAD"))
    private void updateRenderStateHook(EndCrystalEntity endCrystalEntity,
                                       EndCrystalEntityRenderState endCrystalEntityRenderState,
                                       float f,
                                       CallbackInfo info)
    {
        this.last = endCrystalEntity;
    }

    @Inject(
            method = "render(Lnet/minecraft/client/render/entity/state/EndCrystalEntityRenderState;" +
                    "Lnet/minecraft/client/util/math/MatrixStack;" +
                    "Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/render/entity/model/EndCrystalEntityModel;" +
                            "setAngles(Lnet/minecraft/client/render/entity/state/EndCrystalEntityRenderState;)V"))
    private void preModelRenderHook(EndCrystalEntityRenderState endCrystalEntityRenderState,
                                    MatrixStack matrixStack,
                                    VertexConsumerProvider vertexConsumerProvider,
                                    int i,
                                    CallbackInfo info)
    {
        if (ChamsModule.getInstance().isEnabled() && ChamsModule.getInstance().xqz.getValue() && ChamsModule.getInstance().isValid(last))
        {
            END_CRYSTAL = Layers.ENTITY.apply(ChamsModule.getInstance().opacity.getValue() == 0.0f ? BLANK : TEXTURE, true);
            return;
        }

        END_CRYSTAL = RenderLayer.getEntityCutoutNoCull(TEXTURE);
    }

    @Inject(
            method = "render(Lnet/minecraft/client/render/entity/state/EndCrystalEntityRenderState;" +
                    "Lnet/minecraft/client/util/math/MatrixStack;" +
                    "Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/render/entity/model/EndCrystalEntityModel;" +
                            "setAngles(Lnet/minecraft/client/render/entity/state/EndCrystalEntityRenderState;)V",
                    shift = At.Shift.AFTER))
    private void setAnglesHook(EndCrystalEntityRenderState endCrystalEntityRenderState, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo info)
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
