package net.shoreline.client.api.math;

public class NanoTimer implements Timer
{
    private long time;

    public NanoTimer()
    {
        this.time = System.nanoTime();
    }

    @Override
    public boolean hasPassed(Number time)
    {
        return getElapsedTime() > time.longValue();
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

    private long toMillis(long nanos)
    {
        return nanos / 1000000;
    }
}
