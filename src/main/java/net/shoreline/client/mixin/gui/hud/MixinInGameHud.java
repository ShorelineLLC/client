package net.shoreline.client.mixin.gui.hud;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.shoreline.client.impl.event.gui.hud.RenderCrosshairEvent;
import net.shoreline.client.impl.event.gui.hud.RenderOverlayEvent;
import net.shoreline.client.util.Globals;
import net.shoreline.eventbus.EventBus;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author linus
 * @see InGameHud
 * @since 1.0
 */
@Mixin(InGameHud.class)
public class MixinInGameHud implements Globals
{
    @Shadow
    @Final
    private static Identifier PUMPKIN_BLUR;
    //
    @Shadow
    @Final
    private static Identifier POWDER_SNOW_OUTLINE;

    @Inject(method = "render", at = @At(value = "TAIL"))
    private void hookRender(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci)
    {
        RenderOverlayEvent.Post renderOverlayEvent =
                new RenderOverlayEvent.Post(context, tickCounter.getTickDelta(true));
        EventBus.INSTANCE.dispatch(renderOverlayEvent);
    }

//    @Redirect(method = "renderHotbar", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/player/PlayerInventory;selectedSlot:I"))
//    private int hookRenderHotbar$selectedSlot(PlayerInventory instance) {
//        return Managers.INVENTORY.getServerSlot();
//    }

    /**
     * @param context
     * @param ci
     */
    @Inject(method = "renderStatusEffectOverlay", at = @At(value = "HEAD"),
            cancellable = true)
    private void hookRenderStatusEffectOverlay(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci)
    {
        RenderOverlayEvent.StatusEffect renderOverlayEvent =
                new RenderOverlayEvent.StatusEffect(context);
        EventBus.INSTANCE.dispatch(renderOverlayEvent);
        if (renderOverlayEvent.isCanceled())
        {
            ci.cancel();
        }
    }

    /**
     * @param context
     * @param nauseaStrength
     * @param ci
     */
    @Inject(method = "renderPortalOverlay", at = @At("HEAD"), cancellable = true)
    private void hookRenderPortalOverlay(DrawContext context,
                                         float nauseaStrength,
                                         CallbackInfo ci)
    {
        RenderOverlayEvent.Portal renderOverlayEvent = new RenderOverlayEvent.Portal(context);
        EventBus.INSTANCE.dispatch(renderOverlayEvent);
        if (renderOverlayEvent.isCanceled())
        {
            ci.cancel();
        }
    }

    /**
     * @param context
     * @param scale
     * @param ci
     */
    @Inject(method = "renderSpyglassOverlay", at = @At(value = "HEAD"),
            cancellable = true)
    private void hookRenderSpyglassOverlay(DrawContext context, float scale,
                                           CallbackInfo ci)
    {
        RenderOverlayEvent.Spyglass renderOverlayEvent =
                new RenderOverlayEvent.Spyglass(context);
        EventBus.INSTANCE.dispatch(renderOverlayEvent);
        if (renderOverlayEvent.isCanceled())
        {
            ci.cancel();
        }
    }

    /**
     * @param context
     * @param texture
     * @param opacity
     * @param ci
     */
    @Inject(method = "renderOverlay", at = @At(value = "HEAD"), cancellable = true)
    private void hookRenderOverlay(DrawContext context, Identifier texture,
                                   float opacity, CallbackInfo ci)
    {
        if (texture.getPath().equals(PUMPKIN_BLUR.getPath()))
        {
            RenderOverlayEvent.Pumpkin renderOverlayEvent =
                    new RenderOverlayEvent.Pumpkin(context);
            EventBus.INSTANCE.dispatch(renderOverlayEvent);
            if (renderOverlayEvent.isCanceled())
            {
                ci.cancel();
            }
        }
        else if (texture.getPath().equals(POWDER_SNOW_OUTLINE.getPath()))
        {
            RenderOverlayEvent.Frostbite renderOverlayEvent =
                    new RenderOverlayEvent.Frostbite(context);
            EventBus.INSTANCE.dispatch(renderOverlayEvent);
            if (renderOverlayEvent.isCanceled())
            {
                ci.cancel();
            }
        }
    }

    /**
     * @param instance
     * @param text
     * @param x
     * @param y
     * @param color
     * @return
     */
    @Redirect(method = "renderHeldItemTooltip", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/DrawContext;drawTextWithBackground(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/Text;IIII)I"))
    private int hookRenderHeldItemTooltip(DrawContext instance, TextRenderer textRenderer, Text text, int x, int y, int width, int color)
    {
        RenderOverlayEvent.ItemName renderOverlayEvent =
                new RenderOverlayEvent.ItemName(instance);
        EventBus.INSTANCE.dispatch(renderOverlayEvent);
        if (renderOverlayEvent.isCanceled())
        {
            if (renderOverlayEvent.isUpdateXY())
            {
                return instance.drawText(mc.textRenderer, text,
                        renderOverlayEvent.getX(), renderOverlayEvent.getY(), color, true);
            }
            return 0;
        }
        return instance.drawText(mc.textRenderer, text, x, y, color, true);
    }

    @Inject(method = "renderMainHud", at = @At(value = "TAIL"))
    private void hookRenderHotbar(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci)
    {
        RenderOverlayEvent.Hotbar hotbar = new RenderOverlayEvent.Hotbar(context);
        EventBus.INSTANCE.dispatch(hotbar);
    }

    @Inject(method = "renderCrosshair", at = @At(value = "HEAD"), cancellable = true)
    private void hookRenderCrosshair(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci)
    {
        RenderCrosshairEvent renderCrosshairEvent = new RenderCrosshairEvent(context);
        EventBus.INSTANCE.dispatch(renderCrosshairEvent);
        if (renderCrosshairEvent.isCanceled())
        {
            ci.cancel();
        }
    }
}
