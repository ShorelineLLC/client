package net.shoreline.client.impl.render.animation;

import lombok.Getter;
import lombok.Setter;
import net.shoreline.client.impl.render.Easing;

@Getter
public class Animation
{
    @Setter
    private float length;
    private long last;
    private boolean state;

    @Setter
    private Easing easing;

    public Animation(float length)
    {
        this(false, length);
    }

    public Animation(boolean initial, float length)
    {
        this(initial, length, Easing.LINEAR);
    }

    public Animation(boolean initial, float length, Easing easing)
    {
        this.length = length;
        this.state = initial;
        this.easing = easing;
    }

    public void setState(boolean state)
    {
        last = (long) (!state ? System.currentTimeMillis() - ((1 - getFactor()) * length) : System.currentTimeMillis() - (getFactor() * length));
        this.state = state;
    }

    public void setStateHard(boolean state)
    {
        this.state = state;
        if (state)
        {
            this.last = System.currentTimeMillis() - (long) (getLinearFactor() * length);
        }
        else
        {
            this.last = (long) (System.currentTimeMillis() - ((1 - getLinearFactor()) * length));
        }
    }

    public boolean getState()
    {
        return state;
    }

    public double getFactor()
    {
        return easing.ease(getLinearFactor());
    }

    public double getLinearFactor()
    {
        return state ? clamp(((System.currentTimeMillis() - last) / length)) : clamp((1 - (System.currentTimeMillis() - last) / length));
    }

    public double getCurrent()
    {
        return 1 + ((2 - 1)) * getFactor();
    }

    private double clamp(double in)
    {
        return in < 0 ? 0 : Math.min(in, 1);
    }

    public boolean isFinished()
    {
        return !getState() && getFactor() == 0.0 || getState() && getFactor() == 1.0;
    }

    public void reset()
    {
        last = System.currentTimeMillis();
    }
}
