package net.shoreline.client.impl.render.animation;

import lombok.Getter;

@Getter
public class Smoother
{
    private double smoothedValue;

    public double smooth(double original, double smoother, double delta)
    {
        double alpha = 1.0 - Math.exp(-smoother * delta);
        smoothedValue += (original - smoothedValue) * alpha;
        return smoothedValue;
    }

    public void clear()
    {
        smoothedValue = 0.0;
    }
}