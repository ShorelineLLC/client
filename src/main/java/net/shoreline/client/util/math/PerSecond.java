package net.shoreline.client.util.math;

import java.util.concurrent.CopyOnWriteArrayList;

public class PerSecond
{
    private final CopyOnWriteArrayList<Long> counter = new CopyOnWriteArrayList<>();

    public void count()
    {
        counter.add(System.currentTimeMillis() + 1000L);
    }

    public int getPerSecond()
    {
        long time = System.currentTimeMillis();
        while (!counter.isEmpty() && counter.getFirst() != null && counter.getFirst() < time)
        {
            counter.removeFirst();
        }

        return counter.size();
    }
}
