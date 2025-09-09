package net.shoreline.client.util.math;

import net.shoreline.client.api.math.NanoTimer;
import net.shoreline.client.api.math.Timer;

import java.util.Arrays;

public class QueueAverage
{
    private final double[] buf;
    private int idx = 0;
    private int count = 0;
    private double sum = 0.0;

    private final long clearTime;
    private final Timer lastAddTime = new NanoTimer();

    public QueueAverage(int capacity)
    {
        this.buf = new double[capacity];
        this.clearTime = -1;
    }

    public QueueAverage(int capacity, long clearTime)
    {
        this.buf = new double[capacity];
        this.clearTime = clearTime;
    }

    public void add(double v)
    {
        lastAddTime.reset();
        if (count < buf.length)
        {
            buf[idx] = v;
            sum += v;
            count++;
        } else
        {
            sum -= buf[idx];
            buf[idx] = v;
            sum += v;
        }

        if (++idx == buf.length)
        {
            idx = 0;
        }
    }

    public double average()
    {
        if (clearTime != -1 && lastAddTime.hasPassed(clearTime))
        {
            clear();
        }

        return count == 0 ? 0.0 : sum / count;
    }

    public double latest()
    {
        if (count == 0)
        {
            return 0.0;
        }

        if (clearTime != -1 && lastAddTime.hasPassed(clearTime))
        {
            clear();
        }

        int last = idx == 0 ? buf.length - 1 : idx - 1;
        return buf[last];
    }

    public int size()
    {
        return count;
    }

    public void clear()
    {
        idx = 0;
        count = 0;
        sum = 0.0;
        Arrays.fill(buf, 0.0);
    }
}
