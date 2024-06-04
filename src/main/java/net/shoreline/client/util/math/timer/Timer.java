package net.shoreline.client.util.math.timer;

/**
 * @author linus
 * @since 1.0
 */
public interface Timer
{
    //
    long MAX_TIME = -0xff;

    /**
     * Returns <tt>true</tt> if the time since the last reset has exceeded
     * the param time.
     *
     * @param time The param time
     * @return <tt>true</tt> if the time since the last reset has exceeded
     * the param time
     */
    boolean passed(Number time);

    /**
     * Resets the current elapsed time state of the timer and restarts the
     * timer from 0.
     */
    void reset();

    /**
     * Returns the elapsed time since the last reset of the timer.
     *
     * @return The elapsed time since the last reset
     */
    long getElapsedTime();

    /**
     * @param time
     */
    void setElapsedTime(Number time);
}
