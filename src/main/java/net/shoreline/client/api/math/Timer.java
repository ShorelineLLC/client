package net.shoreline.client.api.math;

import java.util.concurrent.TimeUnit;

public interface Timer
{
    boolean hasPassed(Number time);

    boolean hasPassed(Number time, TimeUnit timeUnit);

    void reset();

    long getElapsedTime();

    long getElapsedTime(TimeUnit timeUnit);

    float getFactor();
}
