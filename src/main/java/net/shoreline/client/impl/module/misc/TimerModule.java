package net.shoreline.client.impl.module.misc;

import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.setting.BooleanConfig;
import net.shoreline.client.api.config.setting.NumberConfig;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.module.ToggleModule;
import net.shoreline.client.impl.event.TickEvent;
import net.shoreline.client.impl.event.render.TickCounterEvent;
import net.shoreline.client.impl.module.movement.SpeedModule;
import net.shoreline.client.init.Managers;
import net.shoreline.eventbus.annotation.EventListener;
import net.shoreline.eventbus.event.StageEvent;

import java.text.DecimalFormat;

/**
 * @author linus
 * @since 1.0
 */
public class TimerModule extends ToggleModule
{
    private static TimerModule INSTANCE;

    //
    Config<Float> ticksConfig = register(new NumberConfig<>("Ticks", "The game tick speed", 0.1f, 2.0f, 50.0f));
    Config<Boolean> tpsSyncConfig = register(new BooleanConfig("TPSSync", "Syncs game tick speed to server tick speed", false));
    //
    private float prevTimer = -1.0f;
    private float timer = 1.0f;

    /**
     *
     */
    public TimerModule()
    {
        super("Timer", "Changes the client tick speed", ModuleCategory.MISCELLANEOUS);
    }

    public static TimerModule getInstance()
    {
        if (INSTANCE == null)
        {
            INSTANCE = new TimerModule();
        }
        return INSTANCE;
    }

    @Override
    public String getModuleData()
    {
        DecimalFormat decimal = new DecimalFormat("0.0#");
        return decimal.format(timer);
    }

    @Override
    public void toggle()
    {
        SpeedModule.getInstance().setPrevTimer();
        if (SpeedModule.getInstance().isUsingTimer())
        {
            return;
        }
        super.toggle();
    }

    @Override
    public void onDisable()
    {
        Managers.TICK.setClientTick(1.0f);
    }

    @EventListener
    public void onTick(TickEvent event)
    {
        if (event.getStage() == StageEvent.EventStage.PRE)
        {
            if (SpeedModule.getInstance().isUsingTimer())
            {
                return;
            }
            if (tpsSyncConfig.getValue())
            {
                timer = Math.max(Managers.TICK.getTpsCurrent() / 20.0f, 0.1f);
                return;
            }
            timer = ticksConfig.getValue();
        }
    }

    @EventListener
    public void onTickCounter(TickCounterEvent event)
    {
        if (timer != 1.0f)
        {
            event.cancel();
            event.setTicks(timer);
        }
    }

    /**
     * @return
     */
    public float getTimer()
    {
        return timer;
    }

    /**
     * @param timer
     */
    public void setTimer(float timer)
    {
        prevTimer = this.timer;
        this.timer = timer;
    }

    public void resetTimer()
    {
        if (prevTimer > 0.0f)
        {
            this.timer = prevTimer;
            prevTimer = -1.0f;
        }
    }
}
