package net.shoreline.client.mixin.render.item;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.shoreline.eventbus.bus.EventBus;
import net.shoreline.client.impl.event.render.item.RenderArmEvent;
import net.shoreline.client.impl.event.render.item.RenderFirstPersonEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HeldItemRenderer.class)
public class MixinHeldItemRenderer {

    @Shadow
    @Final
    private EntityRenderDispatcher entityRenderDispatcher;

    @Shadow
    @Final
    private MinecraftClient client;

    /**
     *
     * @param matrices
     * @param vertexConsumers
     * @param light
     * @param arm
     * @param ci
     */
    @Inject(method = "renderArmHoldingItem", at = @At(value = "HEAD"), cancellable = true)
    private void hookRenderArm(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, float equipProgress, float swingProgress, Arm arm, CallbackInfo ci) {
        PlayerEntityRenderer playerEntityRenderer = (PlayerEntityRenderer) entityRenderDispatcher.getRenderer(client.player);
        RenderArmEvent renderArmEvent = new RenderArmEvent(matrices, vertexConsumers, light, equipProgress, swingProgress, arm, playerEntityRenderer);
        EventBus.INSTANCE.dispatch(renderArmEvent);
        if (renderArmEvent.isCanceled()) {
            ci.cancel();
        }
    }

    /**
     * @param player
     * @param tickDelta
     * @param pitch
     * @param hand
     * @param swingProgress
     * @param item
     * @param equipProgress
     * @param matrices
     * @param vertexConsumers
     * @param light
     * @param ci
     */
    @Inject(method = "renderFirstPersonItem", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/render/item/HeldItemRenderer;" +
                    "renderItem(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/" +
                    "item/ItemStack;Lnet/minecraft/client/render/model/json/" +
                    "ModelTransformationMode;ZLnet/minecraft/client/util/math/" +
                    "MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V"))
    private void hookRenderFirstPersonItem(AbstractClientPlayerEntity player, float tickDelta,
                                           float pitch, Hand hand, float swingProgress,
                                           ItemStack item, float equipProgress, MatrixStack matrices,
                                           VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        RenderFirstPersonEvent renderFirstPersonEvent = new RenderFirstPersonEvent(hand, item, equipProgress, matrices);
        EventBus.INSTANCE.dispatch(renderFirstPersonEvent);
    }

//    @Inject(method = "applyEatOrDrinkTransformation", at = @At(value = "HEAD"), cancellable = true)
//    private void hookApplyEatOrDrinkTransformation(MatrixStack matrices, float tickDelta, Arm arm, ItemStack stack, CallbackInfo ci) {
//        ci.cancel();
//        float h;
//        float f = (float) client.player.getItemUseTimeLeft() - tickDelta + 1.0f;
//        float g = f / (float)stack.getMaxUseTime();
//        if (g < 0.8f) {
//            h = MathHelper.abs(MathHelper.cos(f / 4.0f * (float)Math.PI) * 0.1f);
//            matrices.translate(0.0f, h, 0.0f);
//        }
//        h = 1.0f - (float) Math.pow(g, 27.0);
//        int i = arm == Arm.RIGHT ? 1 : -1;
//        matrices.translate(h * 0.6f * (float)i, h * -0.5f, h * 0.0f);
//        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float)i * h * 90.0f));
//        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(h * 10.0f));
//        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((float)i * h * 30.0f));
//    }
}
