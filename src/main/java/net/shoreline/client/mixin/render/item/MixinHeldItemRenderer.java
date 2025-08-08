package net.shoreline.client.mixin.render.item;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.shoreline.client.impl.event.render.item.RenderHeldItemEvent;
import net.shoreline.client.impl.event.render.item.SwingAnimFactorEvent;
import net.shoreline.eventbus.EventBus;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
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

    @ModifyArg(method = "updateHeldItems", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/util/math/MathHelper;clamp(FFF)F",
            ordinal = 2), index = 0)
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
