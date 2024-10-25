package net.shoreline.client.impl.event.entity;

import net.minecraft.block.BlockState;
import net.shoreline.eventbus.annotation.Cancelable;
import net.shoreline.eventbus.event.Event;

@Cancelable
public class SlowMovementEvent extends Event
{
    private final BlockState state;
    private float multiplier = 1.0f;

    public SlowMovementEvent(BlockState state)
    {
        this.state = state;
    }

    public BlockState getState()
    {
        return state;
    }

    public float getMultiplier()
    {
        return multiplier;
    }

    public void setMultiplier(float multiplier)
    {
        this.multiplier = multiplier;
    }
}
