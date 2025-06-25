package net.shoreline.client.impl.module.world;

import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.NumberConfig;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.api.module.Toggleable;
import net.shoreline.client.impl.event.render.RenderTickCounterEvent;
import net.shoreline.eventbus.annotation.EventListener;

import java.text.DecimalFormat;

public class TimerModule extends Toggleable
{
    Config<Float> ticksConfig = new NumberConfig.Builder<Float>("Ticks")
            .setMin(0.1f).setMax(50.0f).setDefaultValue(1.5f)
            .setDescription("The game ticks speed").build();

    private final DecimalFormat decimal = new DecimalFormat("0.0#");

    public TimerModule()
    {
        super("Timer", "Change the game tick speed", GuiCategory.WORLD);
    }

    @Override
    public String getModuleData()
    {
        return decimal.format(ticksConfig.getValue());
    }

    @EventListener
    public void onTickCounter(RenderTickCounterEvent event)
    {
        event.cancel();
        event.setTicks(ticksConfig.getValue());
    }
}
