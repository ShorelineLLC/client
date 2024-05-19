package net.shoreline.client.mixin.gui.screen;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.slot.Slot;
import net.shoreline.client.impl.event.handler.EventBus;
import net.shoreline.client.impl.event.gui.RenderTooltipEvent;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HandledScreen.class)
public class MixinHandledScreen {

    @Shadow
    @Nullable
    protected Slot focusedSlot;

    @Inject(method = "drawMouseoverTooltip", at = @At(value = "HEAD"), cancellable = true)
    private void hookDrawMouseoverTooltip(DrawContext context, int x, int y, CallbackInfo ci) {
        if (focusedSlot == null) {
            return;
        }
        RenderTooltipEvent renderTooltipEvent =
                new RenderTooltipEvent(context, focusedSlot.getStack(), x, y);
        EventBus.EVENT_HANDLER.dispatch(renderTooltipEvent);
        if (renderTooltipEvent.isCanceled()) {
            ci.cancel();
        }
    }
}
