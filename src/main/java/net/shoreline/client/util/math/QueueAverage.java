package net.shoreline.client.util.math;

import java.util.Arrays;

public class QueueAverage
{
    private final double[] buf;
    private int idx = 0;
    private int count = 0;
    private double sum = 0.0;

    public QueueAverage(int capacity)
    {
        this.buf = new double[capacity];
    }

    public void add(double v)
    {
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
        return count == 0 ? 0.0 : sum / count;
    }

    public double latest()
    {
        if (count == 0)
        {
            return 0.0;
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
