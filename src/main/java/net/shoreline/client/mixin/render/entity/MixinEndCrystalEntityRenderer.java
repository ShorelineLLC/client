package net.shoreline.client.mixin.render.entity;

import net.minecraft.client.MinecraftClient;
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
import net.shoreline.client.ShorelineMod;
import net.shoreline.client.impl.imixin.IModel;
import net.shoreline.client.impl.module.render.ChamsModule;
import net.shoreline.client.impl.module.render.ModelsModule;
import net.shoreline.client.impl.render.ChamsRenderer;
import net.shoreline.client.impl.render.ColorUtil;
import net.shoreline.client.impl.render.Layers;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
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
    private static final Identifier BLANK = Identifier.of(ShorelineMod.MOD_ID, "textures/blank.png");

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
        if (ChamsModule.getInstance().isEnabled() && ChamsModule.getInstance().isValid(last))
        {
            END_CRYSTAL = Layers.CRYSTALS.apply(ChamsModule.getInstance().getOpacity() == 0.0f ? BLANK : TEXTURE, true);
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
                    target = "Lnet/minecraft/client/render/entity/model/EndCrystalEntityModel;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;II)V",
                    shift = At.Shift.AFTER))
    private void setAnglesHook(EndCrystalEntityRenderState endCrystalEntityRenderState, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo info)
    {
        if (ChamsRenderer.rendering)
        {
            return;
        }

        boolean valid = ChamsModule.getInstance().isValid(last);
        ((IModel) model).cancelModel(valid);
        if (ChamsModule.getInstance().isEnabled() && valid)
        {
            int color = ChamsModule.getInstance().getColor(last).getRGB();
            if (ChamsModule.getInstance().mode.getValue() == ChamsModule.ChamsMode.SHINE)
            {
                Layers.QUADS_GLINT.startDrawing();
                VertexConsumerProvider provider = MinecraftClient.getInstance().getBufferBuilders().getEffectVertexConsumers();
                VertexConsumer consumer = ItemRenderer.getArmorGlintConsumer(provider, Layers.QUADS_GLINT, true);
                model.render(matrixStack, consumer, i, OverlayTexture.DEFAULT_UV, color);
                Layers.QUADS_GLINT.endDrawing();
            }

            ChamsRenderer renderer = ChamsModule.getInstance().mode.getValue().getRenderer();
            float tickDelta = MinecraftClient.getInstance().getRenderTickCounter().getTickDelta(false);
            ChamsRenderer.render(renderer, last, tickDelta, color);
        }
    }

    @Redirect(
            method = "render(Lnet/minecraft/client/render/entity/state/EndCrystalEntityRenderState;" +
                    "Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/util/math/MatrixStack;scale(FFF)V"))
    private void hookScale(MatrixStack instance, float x, float y, float z)
    {
        float scale = ModelsModule.INSTANCE.isEnabled() ? ModelsModule.INSTANCE.getCrystalScale().getValue() : 1.0f;
        instance.scale(2.0f * scale, 2.0f * scale, 2.0f * scale);
    }
}
