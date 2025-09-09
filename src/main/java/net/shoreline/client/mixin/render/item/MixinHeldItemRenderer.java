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
import net.shoreline.client.impl.event.render.item.RenderHandEvent;
import net.shoreline.client.impl.event.render.item.RenderHeldItemEvent;
import net.shoreline.client.impl.event.render.item.SwingAnimFactorEvent;
import net.shoreline.eventbus.EventBus;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
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

        original.call(instance, player, tickDelta, pitch, hand, swingProgress, item, equipProgress, matrices, event.getVertexConsumerProvider(), light);
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

    @Redirect(method = "applyEatOrDrinkTransformation", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/util/math/MatrixStack;translate(FFF)V", ordinal = 0))
    private void hookApplyEatOrDrinkTransformation(MatrixStack instance, float x, float y, float z)
    {
        RenderHeldItemEvent.Eating renderHeldItemEvent = new RenderHeldItemEvent.Eating();
        EventBus.INSTANCE.dispatch(renderHeldItemEvent);
        if (renderHeldItemEvent.isCanceled())
        {
            y *= renderHeldItemEvent.getFactorY();
        }

        instance.translate(x, y, 0.0f);
    }

    @ModifyArg(method = "updateHeldItems", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/util/math/MathHelper;clamp(FFF)F", ordinal = 2), index = 0)
    private float hookUpdateHeldItems(float value)
    {
        SwingAnimFactorEvent animFactorEvent = new SwingAnimFactorEvent();
        EventBus.INSTANCE.dispatch(animFactorEvent);
        if (animFactorEvent.isCanceled())
        {
            ItemStack itemStack = client.player.getMainHandStack();
            float g = mainHand != itemStack ? 0.0f : 1.0f;
            return g - equipProgressMainHand;
        }

        return value;
    }
}
