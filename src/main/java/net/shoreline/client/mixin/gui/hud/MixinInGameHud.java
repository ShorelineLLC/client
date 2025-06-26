package net.shoreline.client.mixin.gui.hud;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.util.Identifier;
import net.shoreline.client.impl.event.gui.hud.HudOverlayEvent;
import net.shoreline.client.impl.event.gui.hud.OverlayEvent;
import net.shoreline.eventbus.EventBus;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class MixinInGameHud
{
    @Shadow
    @Final
    private static Identifier POWDER_SNOW_OUTLINE;

    @Inject(method = "render", at = @At(value = "RETURN"))
    private void hookRender(DrawContext context,
                            RenderTickCounter tickCounter,
                            CallbackInfo ci)
    {
        EventBus.INSTANCE.dispatch(new HudOverlayEvent.Post(
                context, tickCounter.getTickProgress(true)));
    }

    @Inject(method = "renderPortalOverlay", at = @At("HEAD"), cancellable = true)
    private void hookRenderPortalOverlay(DrawContext context,
                                         float nauseaStrength,
                                         CallbackInfo ci)
    {
        OverlayEvent.Portal renderOverlayEvent = new OverlayEvent.Portal();
        EventBus.INSTANCE.dispatch(renderOverlayEvent);
        if (renderOverlayEvent.isCanceled())
        {
            ci.cancel();
        }
    }

    @Inject(method = "renderSpyglassOverlay", at = @At(value = "HEAD"), cancellable = true)
    private void hookRenderSpyglassOverlay(DrawContext context,
                                           float scale,
                                           CallbackInfo ci)
    {
        OverlayEvent.Spyglass renderOverlayEvent = new OverlayEvent.Spyglass();
        EventBus.INSTANCE.dispatch(renderOverlayEvent);
        if (renderOverlayEvent.isCanceled())
        {
            ci.cancel();
        }
    }

    @Inject(method = "renderOverlay", at = @At(value = "HEAD"), cancellable = true)
    private void hookRenderOverlay(DrawContext context,
                                   Identifier texture,
                                   float opacity,
                                   CallbackInfo ci)
    {
        if (texture.getPath().equals(POWDER_SNOW_OUTLINE.getPath()))
        {
            OverlayEvent.Frostbite renderOverlayEvent = new OverlayEvent.Frostbite();
            EventBus.INSTANCE.dispatch(renderOverlayEvent);
            if (renderOverlayEvent.isCanceled())
            {
                ci.cancel();
            }
        }
    }
}
