package net.shoreline.client.impl.event.entity;

import net.minecraft.entity.LivingEntity;
import net.shoreline.eventbus.annotation.Cancelable;
import net.shoreline.eventbus.event.Event;

@Cancelable
public class StepEvent extends Event
{
    private final LivingEntity entity;
    private float stepHeight;

    public StepEvent(LivingEntity entity, float stepHeight)
    {
        this.entity = entity;
        this.stepHeight = stepHeight;
    }

    public LivingEntity getEntity()
    {
        return entity;
    }

    public void setStepHeight(float stepHeight)
    {
        this.stepHeight = stepHeight;
    }

    public float getStepHeight()
    {
        return stepHeight;
    }
}
