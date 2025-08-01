package net.shoreline.client.impl.event.input;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.util.math.Vec2f;
import net.shoreline.eventbus.Event;
import net.shoreline.eventbus.annotation.Cancelable;

@Cancelable
@Getter
@Setter
public class PlayerInputEvent extends Event
{
    private float movementForward;
    private float movementSideways;

    public PlayerInputEvent(float movementForward, float movementSideways)
    {
        this.movementForward = movementForward;
        this.movementSideways = movementSideways;
    }
}
