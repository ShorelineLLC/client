package net.shoreline.client.mixin.render;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.Pool;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.MathHelper;
import net.shoreline.client.impl.event.entity.player.ReachEvent;
import net.shoreline.client.impl.event.render.*;
import net.shoreline.client.impl.imixin.IGameRenderer;
import net.shoreline.eventbus.EventBus;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;
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

    @Override
    @Invoker("getFov")
    public abstract float invokeGetFov(Camera camera, float tickDelta, boolean changingFov);

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

    @Inject(
            method = "renderWorld",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/systems/RenderSystem;clear(I)V"))
    private void renderWorldHook(RenderTickCounter tickCounter, CallbackInfo info,
                                 @Local(ordinal = 2) Matrix4f matrix4f)
    {
        MatrixStack matrices = new MatrixStack();
        RenderSystem.getModelViewStack().pushMatrix();
        RenderSystem.getModelViewStack().mul(matrix4f);
        RenderSystem.getModelViewStack().mul(matrices.peek().getPositionMatrix().invert());
        RenderEntityWorldEvent.Post renderEntityEvent = new RenderEntityWorldEvent.Post(tickCounter.getTickDelta(true));
        EventBus.INSTANCE.dispatch(renderEntityEvent);
        RenderSystem.getModelViewStack().popMatrix();
    }

    @Inject(method = "renderWorld", at = @At("TAIL"))
    private void renderWorld$TAIL(RenderTickCounter renderTickCounter, CallbackInfo info)
    {
        RenderShaderEvent.Post shaderEvent = new RenderShaderEvent.Post();
        EventBus.INSTANCE.dispatch(shaderEvent);
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

    @Redirect(method = "updateCrosshairTarget", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/hit/EntityHitResult;getEntity()Lnet/minecraft/entity/Entity;"))
    private Entity hookCrosshairTarget(EntityHitResult instance)
    {
        Entity entity = instance.getEntity();
        if (entity != null)
        {
            CrosshairTargetEvent targetEvent = new CrosshairTargetEvent(entity);
            EventBus.INSTANCE.dispatch(targetEvent);
            return targetEvent.isCanceled() ? null : entity;
        }

        return null;
    }

    @Redirect(method = "updateCrosshairTarget", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;getEntityInteractionRange()D"))
    private double hookTargetedEntity(ClientPlayerEntity instance)
    {
        ReachEvent reachEvent = new ReachEvent();
        EventBus.INSTANCE.dispatch(reachEvent);
        double range = instance.getEntityInteractionRange();
        return reachEvent.isCanceled() ? range + reachEvent.getReach() : range;
    }

    @Redirect(method = "updateCrosshairTarget", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;getBlockInteractionRange()D"))
    private double hookTargetedEntity$1(ClientPlayerEntity instance)
    {
        ReachEvent reachEvent = new ReachEvent();
        EventBus.INSTANCE.dispatch(reachEvent);
        double range = instance.getBlockInteractionRange();
        return reachEvent.isCanceled() ? range + reachEvent.getReach() : range;
    }
}
