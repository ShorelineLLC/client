package net.shoreline.client.util.math;

import java.util.LinkedList;
import java.util.Queue;

public class PerSecondCounter
{
    private final Queue<Long> counter = new LinkedList<>();

    public void mark()
    {
        counter.add(System.currentTimeMillis() + 1000L);
    }

    public int getPerSecond()
    {
        long time = System.currentTimeMillis();
        try
        {
            while (!counter.isEmpty() && counter.peek() < time)
            {
                counter.remove();
            }
        } catch (Exception e)
        {
            // empty catch block
        }
        return counter.size();
    }
}
