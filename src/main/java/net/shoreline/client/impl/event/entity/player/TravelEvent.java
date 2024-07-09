package net.shoreline.client.impl.event.entity.player;

import net.minecraft.util.math.Vec3d;
import net.shoreline.eventbus.annotation.Cancelable;
import net.shoreline.eventbus.event.Event;
import net.shoreline.eventbus.event.StageEvent;

@Cancelable
public class TravelEvent extends Event
{
    private final Vec3d movementInput;
    private final boolean pre;

    public TravelEvent(Vec3d movementInput, boolean pre)
    {
        this.movementInput = movementInput;
        this.pre = pre;
    }

    public Vec3d getMovementInput()
    {
        return movementInput;
    }

    public boolean isPre()
    {
        return pre;
    }
}
