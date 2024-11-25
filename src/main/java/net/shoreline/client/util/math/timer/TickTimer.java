package net.shoreline.client.util.math.timer;

import net.shoreline.client.impl.event.TickEvent;
import net.shoreline.eventbus.EventBus;
import net.shoreline.eventbus.annotation.EventListener;
import net.shoreline.eventbus.event.StageEvent;

/**
 * TODO: Test the accuracy of ticks
 *
 * @author linus
 * @see Timer
 * @since 1.0
 */
public class TickTimer implements Timer
{
    //
    private long ticks;

    /**
     *
     */
    public TickTimer()
    {
        ticks = 0;
        EventBus.INSTANCE.subscribe(this);
    }

    /**
     * @param event
     */
    @EventListener(priority = Integer.MAX_VALUE)
    public void onTick(TickEvent event)
    {
        if (event.getStage() == StageEvent.EventStage.PRE)
        {
            ++ticks;
        }
    }

    /**
     * Returns <tt>true</tt> if the time since the last reset has exceeded
     * the param time.
     *
     * @param time The param time
     * @return <tt>true</tt> if the time since the last reset has exceeded
     * the param time
     */
    @Override
    public boolean passed(Number time)
    {
        return ticks >= time.longValue();
    }

    /**
     *
     */
    @Override
    public void reset()
    {
        setElapsedTime(0);
    }

    /**
     * @return
     */
    @Override
    public long getElapsedTime()
    {
        return ticks;
    }

    /**
     * @param time
     */
    @Override
    public void setElapsedTime(Number time)
    {
        ticks = time.longValue();
    }
}
