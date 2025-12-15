package net.shoreline.client.impl.render.animation;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.util.math.MathHelper;
import net.shoreline.client.impl.render.Easing;

@Getter
@Setter
public class UnboundAnimation
{
    private float target;
    private float prev;
    private long lastMillis;
    private Easing easing;

    private final int length;

    public UnboundAnimation(int length, Easing easing)
    {
        this.prev = 0;
        this.target = 0;
        this.length = length;
        this.easing = easing;
        this.lastMillis = System.currentTimeMillis();
    }

    public UnboundAnimation(float prev, float current, int length, Easing easing)
    {
        this.prev = prev;
        this.target = current;
        this.length = length;
        this.easing = easing;
        this.lastMillis = System.currentTimeMillis();
    }

    public float get()
    {
        return (float) MathHelper.lerp(easing.ease(toDelta(lastMillis, length)), prev, target);
    }

    public float get(float target)
    {
        float lerp = (float) MathHelper.lerp(easing.ease(toDelta(lastMillis, length)), prev, this.target);
        if (this.target != target)
        {
            this.prev = lerp;
            this.target = target;
            this.lastMillis = System.currentTimeMillis();
        }

        return lerp;
    }

    public double getFactor()
    {
        long elapsedTime = System.currentTimeMillis() - lastMillis;
        float rawFactor = Math.min(1.0f, Math.max(0.0f, (float) elapsedTime / length));
        return easing.ease(rawFactor);
    }

    public static float toDelta(long start, int length)
    {
        return MathHelper.clamp(toDelta(start) / (float) length, 0.0f, 1.0f);
    }

    public static long toDelta(long start)
    {
        return System.currentTimeMillis() - start;
    }
}