package net.shoreline.client.mixin.render;

import net.minecraft.client.render.Camera;
import net.shoreline.client.impl.event.render.CameraClipEvent;
import net.shoreline.client.impl.event.render.CameraEvent;
import net.shoreline.client.impl.imixin.ICamera;
import net.shoreline.eventbus.EventBus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Camera.class)
public abstract class MixinCamera implements ICamera
{
    @Shadow
    protected abstract void setPos(double x, double y, double z);

    @Shadow
    protected abstract void setRotation(float yaw, float pitch);

    @Override
    @Accessor("cameraY")
    public abstract float getCameraY();

    @Override
    @Accessor("lastCameraY")
    public abstract float getLastCameraY();

    @Shadow
    private float lastTickDelta;

    @Redirect(method = "update", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/render/Camera;setPos(DDD)V"))
    private void hookUpdatePosition(Camera instance, double x, double y, double z)
    {
        CameraEvent.Position cameraPositionEvent = new CameraEvent.Position(x, y, z, lastTickDelta);
        EventBus.INSTANCE.dispatch(cameraPositionEvent);
        if (cameraPositionEvent.isCanceled())
        {
            setPos(cameraPositionEvent.getX(), cameraPositionEvent.getY(), cameraPositionEvent.getZ());
            return;
        }

        setPos(x, y, z);
    }

    @Redirect(method = "update", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/render/Camera;setRotation(FF)V"))
    private void hookUpdateRotation(Camera instance, float yaw, float pitch)
    {
        CameraEvent.Rotation cameraRotationEvent = new CameraEvent.Rotation(yaw, pitch, lastTickDelta);
        EventBus.INSTANCE.dispatch(cameraRotationEvent);
        if (cameraRotationEvent.isCanceled())
        {
            setRotation(cameraRotationEvent.getYaw(), cameraRotationEvent.getPitch());
            return;
        }

        setRotation(yaw, pitch);
    }

    @Inject(method = "clipToSpace", at = @At(value = "HEAD"), cancellable = true)
    private void hookClipToSpace(float f, CallbackInfoReturnable<Float> cir)
    {
        CameraClipEvent cameraClipEvent = new CameraClipEvent();
        EventBus.INSTANCE.dispatch(cameraClipEvent);
        if (cameraClipEvent.isCanceled())
        {
            cir.setReturnValue(cameraClipEvent.getDistance());
            cir.cancel();
        }
    }
}
