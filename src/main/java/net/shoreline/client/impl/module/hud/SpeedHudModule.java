package net.shoreline.client.impl.module.hud;

import net.minecraft.util.Formatting;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.EnumConfig;
import net.shoreline.client.impl.module.impl.hud.DynamicEntry;
import net.shoreline.client.impl.module.impl.hud.DynamicHudModule;
import net.shoreline.client.impl.module.world.TimerModule;

import java.text.DecimalFormat;

public class SpeedHudModule extends DynamicHudModule
{
    Config<Format> formatMode = new EnumConfig.Builder<Format>("Format")
            .setValues(Format.values())
            .setDescription("The speed value format")
            .setDefaultValue(Format.KMH).build();

    public SpeedHudModule()
    {
        super("Speedometer", "Displays the player speed", 200, 250);
    }

    @Override
    public void loadEntries()
    {
        getHudEntries().add(new DynamicEntry(this, this::getSpeedometerText, () -> true));
    }

    private String getSpeedometerText()
    {
        double speed;
        double x = mc.player.getX() - mc.player.prevX;
        // double y = mc.player.getY() - mc.player.prevY;
        double z = mc.player.getZ() - mc.player.prevZ;
        float timer = TimerModule.INSTANCE.getTimerTicks();
        if (formatMode.getValue() == Format.KMH)
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

        String format = formatMode.getValue() == Format.KMH ? "km/h" : "b/s";
        return String.format("Speed " + Formatting.WHITE + "%s%s", DECIMAL_TRIMMED.format(speed), format);
    }

    public enum Format
    {
        KMH,
        BPS
    }
}
