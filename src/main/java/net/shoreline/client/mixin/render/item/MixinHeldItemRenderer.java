package net.shoreline.client.mixin.render.item;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.shoreline.client.impl.event.render.item.RenderHandEvent;
import net.shoreline.client.impl.event.render.item.RenderHeldItemEvent;
import net.shoreline.client.impl.event.render.item.SwingAnimFactorEvent;
import net.shoreline.eventbus.EventBus;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HeldItemRenderer.class)
public class MixinHeldItemRenderer
{
    @Shadow
    @Final
    private MinecraftClient client;

    @Shadow
    private ItemStack mainHand;

    @Shadow
    private float equipProgressMainHand;

    @Inject(method = "renderFirstPersonItem", at = @At(value = "HEAD"), cancellable = true)
    private void hookRenderFirstPersonItem(AbstractClientPlayerEntity player,
                                           float tickDelta,
                                           float pitch,
                                           Hand hand,
                                           float swingProgress,
                                           ItemStack item,
                                           float equipProgress,
                                           MatrixStack matrices,
                                           VertexConsumerProvider vertexConsumers,
                                           int light,
                                           CallbackInfo ci)
    {
        RenderHeldItemEvent.Pre renderFirstPersonEvent = new RenderHeldItemEvent.Pre();
        EventBus.INSTANCE.dispatch(renderFirstPersonEvent);
        if (renderFirstPersonEvent.isCanceled())
        {
            ci.cancel();
        }
    }

    @Inject(method = "renderFirstPersonItem", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/render/item/HeldItemRenderer;renderItem(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ModelTransformationMode;ZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V"))
    private void hookRenderItem(AbstractClientPlayerEntity player,
                                float tickDelta,
                                float pitch,
                                Hand hand,
                                float swingProgress,
                                ItemStack item,
                                float equipProgress,
                                MatrixStack matrices,
                                VertexConsumerProvider vertexConsumers,
                                int light,
                                CallbackInfo ci)
    {
        RenderHeldItemEvent.FirstPerson renderHeldItemEvent = new RenderHeldItemEvent.FirstPerson(matrices, hand);
        EventBus.INSTANCE.dispatch(renderHeldItemEvent);
    }

    @WrapOperation(method = "renderItem(FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;Lnet/minecraft/client/network/ClientPlayerEntity;I)V",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/render/item/HeldItemRenderer;renderFirstPersonItem(Lnet/minecraft/client/network/AbstractClientPlayerEntity;FFLnet/minecraft/util/Hand;FLnet/minecraft/item/ItemStack;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V"))
    private void hookRenderItem(HeldItemRenderer instance,
                                AbstractClientPlayerEntity player,
                                float tickDelta,
                                float pitch,
                                Hand hand,
                                float swingProgress,
                                ItemStack item,
                                float equipProgress,
                                MatrixStack matrices,
                                VertexConsumerProvider vertexConsumers,
                                int light,
                                Operation<Void> original)
    {
        RenderHandEvent event = new RenderHandEvent(vertexConsumers);
        EventBus.INSTANCE.dispatch(event);

        original.call(instance, player, tickDelta, pitch, hand, swingProgress, item, equipProgress, matrices, event.isCanceled() ? event.getVertexConsumerProvider() : vertexConsumers, light);
    }

    @Inject(method = "renderItem(FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;Lnet/minecraft/client/network/ClientPlayerEntity;I)V", at = @At("TAIL"))
    private void hookRenderItemPost(float tickDelta,
                                    MatrixStack matrices,
                                    VertexConsumerProvider.Immediate vertexConsumers,
                                    ClientPlayerEntity player,
                                    int light,
                                    CallbackInfo ci)
    {
        RenderHandEvent.Post event = new RenderHandEvent.Post();
        EventBus.INSTANCE.dispatch(event);
    }

    @Inject(
            method = "applyEatOrDrinkTransformation",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onApplyEatOrDrinkTransformation(MatrixStack matrices,
                                                 float tickDelta,
                                                 Arm arm,
                                                 ItemStack stack,
                                                 PlayerEntity player,
                                                 CallbackInfo ci)
    {
        RenderHeldItemEvent.Eating renderHeldItemEvent = new RenderHeldItemEvent.Eating();
        EventBus.INSTANCE.dispatch(renderHeldItemEvent);
        if (renderHeldItemEvent.isCanceled())
        {
            ci.cancel();
            float var10 = (float) this.client.player.getItemUseTimeLeft() - tickDelta + 1.0F;
            float var11 = var10 / (float) stack.getMaxUseTime(player);
            if (var11 < 0.8F)
            {
                float var9 = MathHelper.abs(
                        MathHelper.cos(var10 / (float) renderHeldItemEvent.getDuration() * (float) Math.PI) * 0.1f
                );

                matrices.translate(0.0F, var9 * renderHeldItemEvent.getFactorY(), 0.0F);
            }

            float var13 = 1.0F - (float) Math.pow(var11, 27.0);
            int var12 = arm == Arm.RIGHT ? 1 : -1;
            matrices.translate(var13 * 0.6F * (float) var12, var13 * -0.5F, var13 * 0.0F);
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float) var12 * var13 * 90.0F));
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(var13 * 10.0F));
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((float) var12 * var13 * 30.0F));
        }
    }

    @Redirect(
            method = "updateHeldItems",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/network/ClientPlayerEntity;getAttackCooldownProgress(F)F"
            )
    )
    private float hookUpdateHeldItems(ClientPlayerEntity instance, float v)
    {
        SwingAnimFactorEvent animFactorEvent = new SwingAnimFactorEvent();
        EventBus.INSTANCE.dispatch(animFactorEvent);
        return animFactorEvent.isCanceled() ? 1.0f : instance.getAttackCooldownProgress(v);
    }
}
