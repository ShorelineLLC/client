package net.shoreline.client.mixin.render.entity;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EndCrystalEntityRenderer;
import net.minecraft.client.render.entity.EnderDragonEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RotationAxis;
import net.shoreline.client.impl.event.render.entity.RenderCrystalEvent;
import net.shoreline.eventbus.EventBus;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EndCrystalEntityRenderer.class)
public class MixinEndCrystalEntityRenderer
{
    //
    @Shadow
    @Final
    private ModelPart core;
    //
    @Shadow
    @Final
    private ModelPart frame;

    @Shadow
    @Final
    private static RenderLayer END_CRYSTAL;

    @Shadow
    @Final
    private ModelPart bottom;

    @Shadow
    @Final
    private static float SINE_45_DEGREES;

    /**
     * @param endCrystalEntity
     * @param f
     * @param g
     * @param matrixStack
     * @param vertexConsumerProvider
     * @param i
     * @param ci
     */
    @Inject(method = "render(Lnet/minecraft/entity/decoration/EndCrystalEntity;" +
            "FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/" +
            "render/VertexConsumerProvider;I)V", at = @At(value = "HEAD"), cancellable = true)
    private void hookRender(EndCrystalEntity endCrystalEntity, float f, float g,
                            MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider,
                            int i, CallbackInfo ci)
    {
        RenderCrystalEvent renderCrystalEvent = new RenderCrystalEvent(endCrystalEntity,
                f, g, matrixStack, i, core, frame);
        // Does it matter if render comes before cancelling?
        EventBus.INSTANCE.dispatch(renderCrystalEvent);
        ci.cancel();
        if (!renderCrystalEvent.isCanceled())
        {
            matrixStack.push();
            float h = !renderCrystalEvent.getBounce() ? -1.0f : EndCrystalEntityRenderer.getYOffset(endCrystalEntity, g);
            float j = (float) ((endCrystalEntity.endCrystalAge + g) * renderCrystalEvent.getSpin()) * 3.0f;
            VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(END_CRYSTAL);
            matrixStack.push();
            matrixStack.scale(renderCrystalEvent.getScale(), renderCrystalEvent.getScale(), renderCrystalEvent.getScale());
            matrixStack.scale(2.0f, 2.0f, 2.0f);
            matrixStack.translate(0.0f, -0.5f, 0.0f);
            int k = OverlayTexture.DEFAULT_UV;
            if (endCrystalEntity.shouldShowBottom()) {
                this.bottom.render(matrixStack, vertexConsumer, i, k);
            }
            matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(j));
            matrixStack.translate(0.0f, 1.5f + h / 2.0f, 0.0f);
            matrixStack.multiply(new Quaternionf().setAngleAxis(1.0471976f, SINE_45_DEGREES, 0.0f, SINE_45_DEGREES));
            this.frame.render(matrixStack, vertexConsumer, i, k);
            float l = 0.875f;
            matrixStack.scale(0.875f, 0.875f, 0.875f);
            matrixStack.multiply(new Quaternionf().setAngleAxis(1.0471976f, SINE_45_DEGREES, 0.0f, SINE_45_DEGREES));
            matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(j));
            this.frame.render(matrixStack, vertexConsumer, i, k);
            matrixStack.scale(0.875f, 0.875f, 0.875f);
            matrixStack.multiply(new Quaternionf().setAngleAxis(1.0471976f, SINE_45_DEGREES, 0.0f, SINE_45_DEGREES));
            matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(j));
            this.core.render(matrixStack, vertexConsumer, i, k);
            matrixStack.scale(1.0f / renderCrystalEvent.getScale(), 1.0f / renderCrystalEvent.getScale(), 1.0f / renderCrystalEvent.getScale());
            matrixStack.pop();
            matrixStack.pop();
            BlockPos blockPos = endCrystalEntity.getBeamTarget();
            if (blockPos != null) {
                float m = (float)blockPos.getX() + 0.5f;
                float n = (float)blockPos.getY() + 0.5f;
                float o = (float)blockPos.getZ() + 0.5f;
                float p = (float)((double)m - endCrystalEntity.getX());
                float q = (float)((double)n - endCrystalEntity.getY());
                float r = (float)((double)o - endCrystalEntity.getZ());
                matrixStack.translate(p, q, r);
                EnderDragonEntityRenderer.renderCrystalBeam(-p, -q + h, -r, g, endCrystalEntity.endCrystalAge, matrixStack, vertexConsumerProvider, i);
            }
        }
    }
}
