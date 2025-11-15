package net.shoreline.client.impl.module.hud;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.shoreline.client.BuildConfig;
import net.shoreline.client.ShorelineMod;
import net.shoreline.client.api.config.BooleanConfig;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.impl.module.impl.hud.HudModule;
import net.shoreline.client.impl.render.ClientFormatting;

public class WatermarkHudModule extends HudModule
{
    Config<Boolean> versionConfig = new BooleanConfig.Builder("Version")
            .setDefaultValue(true).build();

    public WatermarkHudModule()
    {
        super("Watermark", "Displays the client name and version", 2, 2);
    }

    @Override
    public void drawHudComponent(DrawContext context, float tickDelta)
    {
        drawText(context.getMatrices(), getText(), getX() + 2, getY() + 2);
    }

    @Override
    public float getWidth()
    {
        return getTextWidth(getText());
    }

    @Override
    public float getHeight()
    {
        return 12;
    }

    public String getText()
    {
        if (versionConfig.getValue())
        {
            return String.format("%s " + Formatting.WHITE + "%s %s-%s",
                                 ShorelineMod.MOD_NAME,
                                 ShorelineMod.MOD_VER,
                                 BuildConfig.BUILD_IDENTIFIER,
                                 BuildConfig.HASH);
        }

        return ShorelineMod.MOD_NAME;
    }
}
