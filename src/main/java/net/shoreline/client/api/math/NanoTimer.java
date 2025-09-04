package net.shoreline.client.api.math;

import net.minecraft.util.math.MathHelper;

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
        maxTime = time.longValue();
        return getElapsedTime() > maxTime;
    }

    @Override
    public void reset()
    {
        this.time = System.nanoTime();
    }

    @Override
    public long getElapsedTime()
    {
        return toMillis(System.nanoTime() - time);
    }

    @Override
    public float getFactor()
    {
        return MathHelper.clamp(getElapsedTime() / (float) maxTime, 0.0f, 1.0f);
    }

    private long toMillis(long nanos)
    {
        return nanos / 1000000;
    }
}
