package net.shoreline.client.impl.module.world;

import lombok.Getter;
import lombok.Setter;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.EnumConfig;
import net.shoreline.client.api.config.NumberConfig;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.api.module.ListeningToggleable;
import net.shoreline.client.impl.event.TickEvent;
import net.shoreline.client.impl.event.WorldEvent;
import net.shoreline.client.impl.event.render.RenderTickCounterEvent;
import net.shoreline.eventbus.annotation.EventListener;

public class TimerModule extends ListeningToggleable
{
    public static TimerModule INSTANCE;

    Config<TickMode> modeConfig = new EnumConfig.Builder<TickMode>("Mode")
            .setValues(TickMode.values())
            .setDescription("The mode to speed up ticks")
            .setDefaultValue(TickMode.ALWAYS).build();
    Config<Float> ticksConfig = new NumberConfig.Builder<Float>("Ticks")
            .setMin(0.1f).setMax(50.0f).setDefaultValue(1.5f)
            .setDescription("The game ticks speed").build();
    Config<Integer> boostConfig = new NumberConfig.Builder<Integer>("Boost")
            .setMin(5).setMax(60).setDefaultValue(20).setFormat(" ticks")
            .setVisible(() -> modeConfig.getValue() == TickMode.PULSE)
            .setDescription("The max ticks to boost").build();

    private int boostTicks;

    @Getter
    private float timerTicks = 1.0f;
    private float prevTimerTicks = 1.0f;

    public TimerModule()
    {
        super("Timer", "Change the game tick speed", GuiCategory.WORLD);
        INSTANCE = this;
    }

    @Override
    public String getModuleData()
    {
        String ticks = DECIMAL_TRIMMED.format(timerTicks);
        return modeConfig.getValue() == TickMode.PULSE ? boostTicks + ", " + ticks : ticks;
    }

    @Override
    public void onEnable()
    {
        if (modeConfig.getValue() == TickMode.ALWAYS)
        {
            timerTicks = ticksConfig.getValue();
        }
    }

    @Override
    public void onDisable()
    {
        timerTicks = 1.0f;
        prevTimerTicks = 1.0f;
        boostTicks = 0;
    }

    @EventListener
    public void onWorldJoin(WorldEvent.Join event)
    {
        timerTicks = 1.0f;
    }

    @EventListener
    public void onTickPost(TickEvent.Post event)
    {
        if (checkNull())
        {
            return;
        }

        if (modeConfig.getValue() == TickMode.PULSE)
        {
            if (mc.player.getVelocity().horizontalLengthSquared() > 1.0e-7 || !mc.player.isOnGround())
            {
                float ticksBoosted = Math.max(ticksConfig.getValue(), 2.0f) - 1.0f;
                if (boostTicks > 0)
                {
                    boostTicks = Math.max(boostTicks - (int) ticksBoosted, 0);
                    if (timerTicks < ticksConfig.getValue())
                    {
                        prevTimerTicks = timerTicks;
                        timerTicks = ticksConfig.getValue();
                    }

                } else
                {
                    timerTicks = prevTimerTicks;
                }
            }

            else
            {
                if (boostTicks < boostConfig.getValue())
                {
                    ++boostTicks;
                }

                timerTicks = prevTimerTicks;
            }
        }

        else
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

    public void setTimerTicks(float timerTicks)
    {
        this.timerTicks = timerTicks;
        this.prevTimerTicks = timerTicks;
    }

    public enum TickMode
    {
        ALWAYS,
        PULSE
    }
}
