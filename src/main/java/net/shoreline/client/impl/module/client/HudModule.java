package net.shoreline.client.impl.module.client;

import net.shoreline.client.BuildConfig;
import net.shoreline.client.ShorelineMod;
import net.shoreline.client.api.config.BooleanConfig;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.font.FontManager;
import net.shoreline.client.api.font.GlyphBuffer;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.api.module.Toggleable;
import net.shoreline.client.impl.event.gui.hud.HudOverlayEvent;
import net.shoreline.eventbus.annotation.EventListener;

public class HudModule extends Toggleable
{
    Config<Boolean> watermarkConfig = new BooleanConfig.Builder("Watermark")
            .setDescription("Displays client watermark")
            .setNameAliases("Logo")
            .setDefaultValue(true).build();

    private final GlyphBuffer buffer = new GlyphBuffer();

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

            buffer.clear();
            buffer.addString(FontManager.FONT, watermarkText, 0, 0);
            buffer.offsetToTopLeft();

            buffer.draw(event.getContext(), 2, 2);

            // event.getContext().drawText(mc.textRenderer, watermarkText, 2, 2, -1, true);
        }
    }
}
