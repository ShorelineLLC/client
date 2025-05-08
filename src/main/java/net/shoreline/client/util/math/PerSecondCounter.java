package net.shoreline.client.util.math;

import java.util.LinkedList;

public class PerSecondCounter
{
    private final LinkedList<Long> counter = new LinkedList<>();

    public void updateCounter()
    {
        counter.add(System.currentTimeMillis() + 1000L);
    }

    public int getPerSecond()
    {
        long time = System.currentTimeMillis();
        try
        {
            while (!counter.isEmpty() && counter.peek() != null && counter.peek() < time)
            {
                counter.remove();
            }
        } catch (Exception e)
        {
            counter.clear();
            e.printStackTrace();
        }
        return counter.size();
    }
}
