package net.shoreline.client.api.math;

import net.minecraft.util.math.MathHelper;

import java.util.concurrent.TimeUnit;

public class NanoTimer implements Timer
{
    private long maxTime;
    private long time;

    public NanoTimer()
    {
        this.time = System.nanoTime();
    }

    @Override
    public boolean hasPassed(Number time)
    {
        return hasPassed(time, TimeUnit.MILLISECONDS);
    }

    @Override
    public boolean hasPassed(Number time, TimeUnit unit)
    {
        maxTime = time.longValue();
        return getElapsedTime(unit) > maxTime;
    }

    @Override
    public void reset()
    {
        this.time = System.nanoTime();
    }

    @Override
    public long getElapsedTime()
    {
        return getElapsedTime(TimeUnit.MILLISECONDS);
    }

    @Override
    public long getElapsedTime(TimeUnit timeUnit)
    {
        return timeUnit.convert(System.nanoTime() - time, TimeUnit.NANOSECONDS);
    }

    @Override
    public float getFactor()
    {
        return MathHelper.clamp(getElapsedTime() / (float) maxTime, 0.0f, 1.0f);
    }
}
