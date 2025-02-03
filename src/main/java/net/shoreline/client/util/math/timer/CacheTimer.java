package net.shoreline.client.util.math.timer;

import java.util.concurrent.TimeUnit;

/**
 * @author linus
 * @since 1.0
 */
public class CacheTimer implements Timer
{
    // The cached time since last reset which indicates the time passed since
    // the last timer reset
    private long time;

    private long lastResetTime;

    /**
     * Default constructor which will initialize the time to the current time
     * which means {@link #passed(Number)} and {@link #passed(Number, TimeUnit)}
     * will always return <tt>true</tt> initially
     */
    public CacheTimer()
    {
        this.time = System.nanoTime();
    }

    /**
     * Returns <tt>true</tt> if the time since the last reset has exceeded
     * the param time.
     *
     * @param time The param time in ms
     * @return <tt>true</tt> if the time since the last reset has exceeded
     * the param time
     */
    @Override
    public boolean passed(Number time)
    {
        if (time.longValue() <= 0)
        {
            return true;
        }
        return getElapsedTime() > time.longValue();
    }

    /**
     * Returns <tt>true</tt> if the time since the last reset has exceeded
     * the param time which is in the param units.
     *
     * @param time The param time
     * @param unit The unit of the time
     * @return <tt>true</tt> if the time since the last reset has exceeded
     * the param time
     * @see #passed(Number)
     */
    public boolean passed(Number time, TimeUnit unit)
    {
        return passed(unit.toMillis(time.longValue()));
    }

    /**
     * @return
     */
    @Override
    public long getElapsedTime()
    {
        return toMillis(System.nanoTime() - time);
    }

    /**
     * @param time
     */
    @Override
    public void setElapsedTime(Number time)
    {
        this.time = time.longValue() == MAX_TIME ? 0 :
                System.nanoTime() - time.longValue();
    }

    public void setDelay(Number delay)
    {
        this.time += delay.longValue();
    }

    /**
     * @return
     */
    public long getElapsedTime(TimeUnit unit)
    {
        return unit.convert(getElapsedTime(), TimeUnit.MILLISECONDS);
    }

    public long getLastResetTime()
    {
        return lastResetTime;
    }

    /**
     * Sets the cached time since the last reset to the current time
     */
    @Override
    public void reset()
    {
        long time = System.nanoTime();
        lastResetTime = time - this.time;

        this.time = time;
    }

    /**
     * @return
     */
    private long toMillis(long nanos)
    {
        return nanos / 1000000;
    }
}
