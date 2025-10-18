package net.shoreline.client.impl.module.world;

import lombok.Getter;
import lombok.Setter;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.NumberConfig;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.api.module.ListeningToggleable;
import net.shoreline.client.impl.event.TickEvent;
import net.shoreline.client.impl.event.WorldEvent;
import net.shoreline.client.impl.event.render.RenderTickCounterEvent;
import net.shoreline.eventbus.annotation.EventListener;

import java.text.DecimalFormat;

public class TimerModule extends ListeningToggleable
{
    public static TimerModule INSTANCE;

    Config<Float> ticksConfig = new NumberConfig.Builder<Float>("Ticks")
            .setMin(0.1f).setMax(50.0f).setDefaultValue(1.5f)
            .setDescription("The game ticks speed").build();

    private final DecimalFormat decimal = new DecimalFormat("0.0#");

    @Getter
    @Setter
    private float timerTicks = 1.0f;

    public TimerModule()
    {
        super("Timer", "Change the game tick speed", GuiCategory.WORLD);
        INSTANCE = this;
    }

    @Override
    public String getModuleData()
    {
        return decimal.format(timerTicks);
    }

    @Override
    public void onEnable()
    {
        timerTicks = ticksConfig.getValue();
    }

    @Override
    public void onDisable()
    {
        timerTicks = 1.0f;
    }

    @EventListener
    public void onWorldJoin(WorldEvent.Join event)
    {
        timerTicks = 1.0f;
    }

    @EventListener
    public void onTickPre(TickEvent.Pre event)
    {
        if (!checkNull())
        {
            timerTicks = ticksConfig.getValue();
        }
    }

    @EventListener
    public void onTickCounter(RenderTickCounterEvent event)
    {
        event.cancel();
        event.setTicks(timerTicks);
    }

    public enum TickMode
    {
        ALWAYS,
        PULSE
    }
}
