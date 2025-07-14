package net.shoreline.client.mixin.toast;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.toast.ToastManager;
import net.shoreline.client.impl.event.toast.RenderGuiToastEvent;
import net.shoreline.eventbus.EventBus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ToastManager.class)
public class MixinToastManager
{
    @Inject(method = "draw", at = @At(value = "HEAD"), cancellable = true)
    private void hookDraw(DrawContext context,
                          CallbackInfo ci)
    {
        RenderGuiToastEvent renderToastEvent = new RenderGuiToastEvent();
        EventBus.INSTANCE.dispatch(renderToastEvent);
        if (renderToastEvent.isCanceled())
        {
            ci.cancel();
        }
    }
}
