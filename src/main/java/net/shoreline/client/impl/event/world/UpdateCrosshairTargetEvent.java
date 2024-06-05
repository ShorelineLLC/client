package net.shoreline.client.impl.event.world;

import net.minecraft.entity.Entity;
import net.shoreline.eventbus.event.Event;

/**
 * @author xgraza
 * @since 03/29/24
 */
public final class UpdateCrosshairTargetEvent extends Event
{

    private final float tickDelta;
    private final Entity cameraEntity;

    public UpdateCrosshairTargetEvent(float tickDelta, Entity cameraEntity)
    {
        this.tickDelta = tickDelta;
        this.cameraEntity = cameraEntity;
    }

    public float getTickDelta()
    {
        return tickDelta;
    }

    public Entity getCameraEntity()
    {
        return cameraEntity;
    }
}
