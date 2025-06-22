package net.shoreline.client.impl.event.network;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.util.math.Vec3d;
import net.shoreline.eventbus.Event;
import net.shoreline.eventbus.annotation.Cancelable;

@Cancelable
@Getter
@Setter
public class ExplosionEvent extends Event
{
    private Vec3d playerVelocity;

    public ExplosionEvent(Vec3d playerVelocity)
    {
        this.playerVelocity = playerVelocity;
    }
}
