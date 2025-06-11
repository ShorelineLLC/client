package net.shoreline.client.mixin.gui.hud;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.shoreline.client.impl.event.gui.hud.HudOverlayEvent;
import net.shoreline.eventbus.EventBus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class MixinInGameHud
{
    @Inject(method = "render", at = @At(value = "TAIL"))
    private void hookRender(DrawContext context,
                            RenderTickCounter tickCounter,
                            CallbackInfo ci)
    {
        EventBus.INSTANCE.dispatch(new HudOverlayEvent.Post(
                context, tickCounter.getTickProgress(true)));
    }
}
