package net.shoreline.client.impl.module.hud;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.shoreline.client.BuildConfig;
import net.shoreline.client.ShorelineMod;
import net.shoreline.client.impl.module.impl.HudModule;

public class WatermarkHudModule extends HudModule
{
    public WatermarkHudModule()
    {
        super("Watermark", "Displays the client name and version", 2, 2);
    }

    @Override
    public void drawHudComponent(DrawContext context, float tickDelta)
    {
        drawText(context, Text.of(getWatermarkText()), getX() + 2, getY() + 2);
    }

    @Override
    public int getWidth()
    {
        return getTextWidth(Text.of(getWatermarkText()));
    }

    @Override
    public int getHeight()
    {
        return 12;
    }

    private String getWatermarkText()
    {
        return String.format("%s %s (%s%s%s)",
                ShorelineMod.MOD_NAME, ShorelineMod.MOD_VER, BuildConfig.BUILD_IDENTIFIER,
                !BuildConfig.BUILD_IDENTIFIER.equals("dev") ? "-" + BuildConfig.BUILD_NUMBER : "",
                !BuildConfig.HASH.equals("null") ? "-" + BuildConfig.HASH : "");
    }
}
