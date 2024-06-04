package net.shoreline.client.impl.event.entity.player;

import net.minecraft.util.math.Vec3d;
import net.shoreline.eventbus.Cancelable;
import net.shoreline.eventbus.StageEvent;

@Cancelable
public class TravelEvent extends StageEvent
{
    private final Vec3d movementInput;

    public TravelEvent(Vec3d movementInput)
    {
        this.movementInput = movementInput;
    }

    public Vec3d getMovementInput()
    {
        return movementInput;
    }
}
