package net.shoreline.client.mixin.render;

import net.minecraft.client.render.Camera;
import net.shoreline.client.impl.event.render.CameraClipEvent;
import net.shoreline.eventbus.EventBus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Camera.class)
public class MixinCamera
{
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
