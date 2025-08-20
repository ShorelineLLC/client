package net.shoreline.client.mixin.render;

import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.ObjectAllocator;
import net.minecraft.client.util.Pool;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.shoreline.client.impl.event.render.*;
import net.shoreline.client.impl.imixin.IGameRenderer;
import net.shoreline.eventbus.EventBus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public abstract class MixinGameRenderer implements IGameRenderer
{
    @Override
    @Accessor("pool")
    public abstract Pool getPool();

    @Inject(method = "tiltViewWhenHurt", at = @At(value = "HEAD"), cancellable = true)
    private void hookTiltViewWhenHurt(MatrixStack matrices,
                                      float tickDelta,
                                      CallbackInfo ci)
    {
        TiltViewEvent hurtCamEvent = new TiltViewEvent();
        EventBus.INSTANCE.dispatch(hurtCamEvent);
        if (hurtCamEvent.isCanceled())
        {
            ci.cancel();
        }
    }

    @Inject(method = "renderWorld", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/render/WorldRenderer;render(Lnet/minecraft/client/util/ObjectAllocator;Lnet/minecraft/client/render/RenderTickCounter;ZLnet/minecraft/client/render/Camera;Lnet/minecraft/client/render/GameRenderer;Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;)V",
            shift = At.Shift.AFTER))
    private void hookRenderWorldSwap(RenderTickCounter tickCounter,
                                     CallbackInfo info)
    {
        RenderShaderEvent.Post shaderEvent = new RenderShaderEvent.Post();
        RenderEntityWorldEvent.Post renderEntityEvent = new RenderEntityWorldEvent.Post();
        EventBus.INSTANCE.dispatch(shaderEvent);
        EventBus.INSTANCE.dispatch(renderEntityEvent);
    }

    @Redirect(method = "renderWorld", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/util/math/MathHelper;lerp(FFF)F"))
    private float hookLerpNausea(float delta,
                                 float start,
                                 float end)
    {
        RenderNauseaEvent renderNauseaEvent = new RenderNauseaEvent();
        EventBus.INSTANCE.dispatch(renderNauseaEvent);
        return renderNauseaEvent.isCanceled() ? 0.0f : MathHelper.lerp(delta, start, end);
    }

    @Inject(method = "showFloatingItem", at = @At(value = "HEAD"), cancellable = true)
    private void hookShowFloatingItem(ItemStack floatingItem, CallbackInfo ci)
    {
        RenderFloatingItemEvent renderFloatingItemEvent =
                new RenderFloatingItemEvent(floatingItem);
        EventBus.INSTANCE.dispatch(renderFloatingItemEvent);
        if (renderFloatingItemEvent.isCanceled())
        {
            ci.cancel();
        }
    }

    @Inject(method = "shouldRenderBlockOutline", at = @At(value = "HEAD"), cancellable = true)
    private void hookShouldRenderBlockOutline(CallbackInfoReturnable<Boolean> cir)
    {
        RenderBlockOutlineEvent renderBlockOutlineEvent = new RenderBlockOutlineEvent();
        EventBus.INSTANCE.dispatch(renderBlockOutlineEvent);
        if (renderBlockOutlineEvent.isCanceled())
        {
            cir.setReturnValue(false);
            cir.cancel();
        }
    }
}
