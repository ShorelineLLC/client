package net.shoreline.client.impl.module.hud;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.shoreline.client.BuildConfig;
import net.shoreline.client.ShorelineMod;
import net.shoreline.client.impl.module.impl.hud.HudModule;

public class WatermarkHudModule extends HudModule
{

    public WatermarkHudModule()
    {
        super("Watermark", "Displays the client name and version", 2, 2);
    }

    @Override
    public void drawHudComponent(DrawContext context, float tickDelta)
    {
        drawText(context.getMatrices(), ShorelineMod.getFormattedVersion(), getX() + 2, getY() + 2);
    }

    @Override
    public int getWidth()
    {
        return getTextWidth(ShorelineMod.getFormattedVersion());
    }

    @Override
    public int getHeight()
    {
        return 12;
    }
}
