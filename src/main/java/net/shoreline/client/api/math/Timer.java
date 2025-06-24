package net.shoreline.client.api.math;

public interface Timer
{
    boolean hasPassed(Number time);

    void reset();

    long getElapsedTime();
}
