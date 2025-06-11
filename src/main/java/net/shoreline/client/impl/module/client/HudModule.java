package net.shoreline.client.impl.module.client;

import net.shoreline.client.BuildConfig;
import net.shoreline.client.ShorelineMod;
import net.shoreline.client.api.module.Concurrent;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.impl.event.gui.hud.HudOverlayEvent;
import net.shoreline.eventbus.annotation.EventListener;

public class HudModule extends Concurrent
{

    public HudModule() {
        super("HUD", "Heads up display", GuiCategory.CLIENT);
    }

    @EventListener
    public void onHudOverlay(HudOverlayEvent.Post event)
    {
        String watermarkText = String.format("%s %s (%s%s%s)",
                ShorelineMod.MOD_NAME, ShorelineMod.MOD_VER, BuildConfig.BUILD_IDENTIFIER,
                !BuildConfig.BUILD_IDENTIFIER.equals("dev") ? "-" + BuildConfig.BUILD_NUMBER : "",
                !BuildConfig.HASH.equals("null") ? "-" + BuildConfig.HASH : "");

        event.getContext().drawText(mc.textRenderer, watermarkText, 2, 2, -1, true);
    }
}
