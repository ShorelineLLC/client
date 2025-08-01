package net.shoreline.client.mixin.render;

import net.minecraft.client.gl.SimpleFramebuffer;
import net.minecraft.client.render.LightmapTextureManager;
import net.shoreline.client.impl.event.render.WorldGammaEvent;
import net.shoreline.eventbus.EventBus;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LightmapTextureManager.class)
public class MixinLightmapTextureManager
{
    @Shadow @Final private SimpleFramebuffer lightmapFramebuffer;

    @Inject(
            method = "update",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gl/SimpleFramebuffer;endWrite()V"))
    private void hookEndWrite(float delta, CallbackInfo info)
    {
        final WorldGammaEvent event = new WorldGammaEvent();
        EventBus.INSTANCE.dispatch(event);
        if (event.isCanceled())
        {
            lightmapFramebuffer.clear();
        }
    }
}
