package net.shoreline.client.impl.event.network;

import net.shoreline.eventbus.annotation.Cancelable;
import net.shoreline.eventbus.event.Event;

@Cancelable
public class MountJumpStrengthEvent extends Event
{
    //
    private float jumpStrength;

    public float getJumpStrength()
    {
        return jumpStrength;
    }

    public void setJumpStrength(float jumpStrength)
    {
        this.jumpStrength = jumpStrength;
    }
}
