package net.shoreline.client.impl.event.network;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.entity.MovementType;
import net.minecraft.util.math.Vec3d;
import net.shoreline.eventbus.Event;
import net.shoreline.eventbus.annotation.Cancelable;

@Cancelable
@Getter
@Setter
public class PlayerMoveEvent extends Event
{
    private MovementType type;
    private Vec3d movement;

    public PlayerMoveEvent(MovementType type, Vec3d movement)
    {
        this.type = type;
        this.movement = movement;
    }
}
