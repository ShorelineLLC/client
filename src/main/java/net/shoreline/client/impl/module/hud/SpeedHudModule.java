package net.shoreline.client.impl.module.hud;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.EnumConfig;
import net.shoreline.client.impl.module.impl.hud.HudModule;
import net.shoreline.client.impl.module.world.TimerModule;

import java.text.DecimalFormat;

public class SpeedHudModule extends HudModule
{
    Config<Format> formatMode = new EnumConfig.Builder<Format>("Format")
            .setValues(Format.values())
            .setDescription("The speed value format")
            .setDefaultValue(Format.K_M_H).build();

    private final DecimalFormat decimalFormatter = new DecimalFormat("0.0#");

    public SpeedHudModule()
    {
        super("Speed", "Displays the player speed", 200, 250);
    }

    @Override
    public void drawHudComponent(DrawContext context, float tickDelta)
    {
        drawText(context.getMatrices(), getSpeedometerText(), getX() + 2, getY() + 2);
    }

    @Override
    public int getWidth()
    {
        return getTextWidth(getSpeedometerText());
    }

    @Override
    public int getHeight()
    {
        return 12;
    }

    private String getSpeedometerText()
    {
        double speed;
        double x = mc.player.getX() - mc.player.prevX;
        // double y = mc.player.getY() - mc.player.prevY;
        double z = mc.player.getZ() - mc.player.prevZ;
        float timer = TimerModule.INSTANCE.isEnabled() ? TimerModule.INSTANCE.getTimerTicks() : 1.0f;
        if (formatMode.getValue() == Format.K_M_H)
        {
            double dist = Math.sqrt(x * x + z * z) / 1000.0;
            double div = 0.05 / 3600.0;
            speed = dist / div * timer;
        } else
        {
            x *= 20.0;
            z *= 20.0;
            double dist = Math.sqrt(x * x + z * z);
            speed = Math.abs(dist) * timer;
        }

        String format = formatMode.getValue() == Format.K_M_H ? "km/h" : "b/s";
        return String.format("Speed §f%s%s", decimalFormatter.format(speed), format);
    }

    public enum Format
    {
        K_M_H,
        B_P_S
    }
}
