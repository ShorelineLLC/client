package net.shoreline.client.mixin.render;

import net.minecraft.block.enums.CameraSubmersionType;
import net.minecraft.client.render.Camera;
import net.shoreline.client.impl.event.camera.CameraPositionEvent;
import net.shoreline.client.impl.event.camera.CameraRotationEvent;
import net.shoreline.client.impl.event.gui.hud.RenderOverlayEvent;
import net.shoreline.client.impl.event.render.CameraClipEvent;
import net.shoreline.eventbus.EventBus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * @author linus
 * @since 1.0
 */
@Mixin(Camera.class)
public abstract class MixinCamera
{
    @Shadow
    private float lastTickDelta;

    @Shadow
    protected abstract void setPos(double x, double y, double z);

    @Shadow
    protected abstract void setRotation(float yaw, float pitch);

    /**
     * @param cir
     */
    @Inject(method = "getSubmersionType", at = @At(value = "HEAD"),
            cancellable = true)
    private void hookGetSubmersionType(CallbackInfoReturnable<CameraSubmersionType> cir)
    {
        RenderOverlayEvent.Water renderOverlayEvent =
                new RenderOverlayEvent.Water(null);
        EventBus.INSTANCE.dispatch(renderOverlayEvent);
        if (renderOverlayEvent.isCanceled())
        {
            cir.setReturnValue(CameraSubmersionType.NONE);
            cir.cancel();
        }
    }

    /**
     * @param f
     * @param cir
     */
    @Inject(method = "clipToSpace", at = @At(value = "HEAD"), cancellable = true)
    private void hookClipToSpace(float f, CallbackInfoReturnable<Float> cir)
    {
        CameraClipEvent cameraClipEvent =
                new CameraClipEvent(f);
        EventBus.INSTANCE.dispatch(cameraClipEvent);
        if (cameraClipEvent.isCanceled())
        {
            cir.setReturnValue(cameraClipEvent.getDistance());
            cir.cancel();
        }
    }

    @Redirect(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Camera;setPos(DDD)V"))
    private void hookUpdatePosition(Camera instance, double x, double y, double z)
    {
        CameraPositionEvent cameraPositionEvent = new CameraPositionEvent(x, y, z, lastTickDelta);
        EventBus.INSTANCE.dispatch(cameraPositionEvent);
        setPos(cameraPositionEvent.getX(), cameraPositionEvent.getY(), cameraPositionEvent.getZ());

    }

    @Redirect(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Camera;setRotation(FF)V"))
    private void hookUpdateRotation(Camera instance, float yaw, float pitch)
    {
        CameraRotationEvent cameraRotationEvent = new CameraRotationEvent(yaw, pitch, lastTickDelta);
        EventBus.INSTANCE.dispatch(cameraRotationEvent);
        setRotation(cameraRotationEvent.getYaw(), cameraRotationEvent.getPitch());
    }
}
