package net.shoreline.client.impl.world;

import net.shoreline.client.api.GenericFeature;
import net.shoreline.client.impl.event.TickEvent;
import net.shoreline.eventbus.EventBus;
import net.shoreline.eventbus.annotation.EventListener;

public class FallDistManager extends GenericFeature
{
    private double lastY = Double.NaN;
    private float accumulated = 0.0f;

    public FallDistManager()
    {
        super("FallDistance");
        EventBus.INSTANCE.subscribe(this);
    }

    public void reset()
    {
        lastY = Double.NaN;
        accumulated = 0.0f;
    }

    @EventListener
    public void onTick(TickEvent.Post event)
    {
        if (checkNull())
        {
            reset();
            return;
        }

        if (mc.player.isOnGround()
                || mc.player.isTouchingWater()
                || mc.player.isSubmergedInWater()
                || mc.player.isClimbing()
                || mc.player.isGliding()
                || mc.player.getAbilities().flying)
        {
            lastY = mc.player.getY();
            accumulated = 0.0f;
            return;
        }

        if (Double.isNaN(lastY))
        {
            lastY = mc.player.getY();
            accumulated = 0.0f;
            return;
        }

        double dy = mc.player.getY() - lastY;
        if (dy < 0.0)
        {
            accumulated += (float) -dy;
        }

        lastY = mc.player.getY();
    }

    public float getFallDistance()
    {
        return accumulated;
    }
}

