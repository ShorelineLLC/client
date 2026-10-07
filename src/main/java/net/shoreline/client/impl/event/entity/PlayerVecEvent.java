package net.shoreline.client.impl.event.entity;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.util.math.Vec3d;
import net.shoreline.eventbus.Event;
import net.shoreline.eventbus.annotation.Cancelable;

@Getter
@Setter
public class PlayerVecEvent extends Event
{
    private final float tickDelta;
    private Vec3d vec;

    public PlayerVecEvent(float tickDelta, Vec3d vec)
    {
        this.tickDelta = tickDelta;
        this.vec = vec;
    }

    @Cancelable
    public static class Rotation extends PlayerVecEvent {
        public Rotation(float tickDelta, Vec3d rotationVec) {
            super(tickDelta, rotationVec);
        }
    }

    @Cancelable
    public static class Camera extends PlayerVecEvent {
        public Camera(float tickDelta, Vec3d vec) {
            super(tickDelta, vec);
        }
    }
}
