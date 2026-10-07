package net.shoreline.client.mixin.gui.hud;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.shoreline.client.impl.event.gui.hud.HudOverlayEvent;
import net.shoreline.client.impl.event.gui.hud.OverlayEvent;
import net.shoreline.client.impl.event.gui.hud.RenderHotbarItemEvent;
import net.shoreline.client.impl.event.gui.hud.RenderTabEvent;
import net.shoreline.client.impl.module.misc.BetterTabModule;
import net.shoreline.eventbus.EventBus;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(InGameHud.class)
public class MixinInGameHud
{
    @Shadow
    @Final
    private static Identifier POWDER_SNOW_OUTLINE;

    @Redirect(
            method = "renderPlayerList",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/option/KeyBinding;isPressed()Z"))
    private boolean shouldRenderPlayerListHook(KeyBinding instance)
    {
        RenderTabEvent event = new RenderTabEvent();
        event.setPressed(instance.isPressed());
        EventBus.INSTANCE.dispatch(event);
        if (event.isCanceled())
        {
            return event.isPressed();
        }

        return instance.isPressed();
    }

    @ModifyArgs(method = "renderHotbar", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/hud/InGameHud;renderHotbarItem(Lnet/minecraft/client/gui/DrawContext;IILnet/minecraft/client/render/RenderTickCounter;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/item/ItemStack;I)V",
            ordinal = 0))
    private void hookRenderHotbarItem(Args args)
    {
        ItemStack stack = args.get(5);
        int seed = (Integer) args.get(6) - 1;

        RenderHotbarItemEvent event = new RenderHotbarItemEvent(seed, stack);
        EventBus.INSTANCE.dispatch(event);
        if (event.isCanceled())
        {
            args.set(5, event.getStack());
        }
    }

    @Inject(method = "render", at = @At(value = "RETURN"))
    private void hookRender(DrawContext context,
                            RenderTickCounter tickCounter,
                            CallbackInfo ci)
    {
        EventBus.INSTANCE.dispatch(new HudOverlayEvent.Post(
                context, tickCounter.getTickDelta(true)));
    }

    @Inject(method = "renderStatusEffectOverlay", at = @At(value = "HEAD"), cancellable = true)
    private void hookRenderStatusEffectOverlay(DrawContext context,
                                               RenderTickCounter tickCounter,
                                               CallbackInfo ci)
    {
        HudOverlayEvent.Potions event = new HudOverlayEvent.Potions();
        EventBus.INSTANCE.dispatch(event);
        if (event.isCanceled())
        {
            ci.cancel();
        }
    }

    @Inject(method = "renderHeldItemTooltip", at = @At(value = "HEAD"), cancellable = true)
    private void hookRenderHeldItemTooltip(DrawContext context,
                                           CallbackInfo ci)
    {
        HudOverlayEvent.ItemName event = new HudOverlayEvent.ItemName();
        EventBus.INSTANCE.dispatch(event);
        if (event.isCanceled())
        {
            ci.cancel();
        }
    }

    @Inject(method = "renderPortalOverlay", at = @At(value = "HEAD"), cancellable = true)
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

    @Inject(method = "renderCrosshair", at = @At(value = "HEAD"), cancellable = true)
    private void hookRenderCrosshair(DrawContext context,
                                     RenderTickCounter tickCounter,
                                     CallbackInfo ci)
    {
        HudOverlayEvent.Crosshair renderCrosshairEvent = new HudOverlayEvent.Crosshair(context);
        EventBus.INSTANCE.dispatch(renderCrosshairEvent);
        if (renderCrosshairEvent.isCanceled())
        {
            ci.cancel();
        }
    }
}
