package net.shoreline.client.impl.module.client;

import net.minecraft.text.Text;
import net.shoreline.client.BuildConfig;
import net.shoreline.client.ShorelineMod;
import net.shoreline.client.api.config.BooleanConfig;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.impl.event.gui.hud.HudOverlayEvent;
import net.shoreline.client.impl.module.impl.RenderModule;
import net.shoreline.eventbus.annotation.EventListener;

public class HudModule extends RenderModule
{
    Config<Boolean> watermarkConfig = new BooleanConfig.Builder("Watermark")
            .setDescription("Displays client watermark")
            .setNameAliases("Logo")
            .setDefaultValue(true).build();

    public HudModule()
    {
        super("HUD", "Heads up display", GuiCategory.CLIENT);
    }

    @EventListener
    public void onHudOverlay(HudOverlayEvent.Post event)
    {
        if (watermarkConfig.getValue())
        {
            String watermarkText = String.format("%s %s (%s%s%s)",
                    ShorelineMod.MOD_NAME, ShorelineMod.MOD_VER, BuildConfig.BUILD_IDENTIFIER,
                    !BuildConfig.BUILD_IDENTIFIER.equals("dev") ? "-" + BuildConfig.BUILD_NUMBER : "",
                    !BuildConfig.HASH.equals("null") ? "-" + BuildConfig.HASH : "");

            drawText(event.getContext(), Text.literal(watermarkText), 2, 2);
        }
    }
}
