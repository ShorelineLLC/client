package net.shoreline.client.impl.event.render;

import lombok.Getter;
import lombok.Setter;
import net.shoreline.eventbus.Event;
import net.shoreline.eventbus.annotation.Cancelable;

@Getter
public class CameraEvent extends Event
{
    private final float tickDelta;

    public CameraEvent(float tickDelta)
    {
        this.tickDelta = tickDelta;
    }

    @Cancelable
    @Getter
    @Setter
    public static class Position extends CameraEvent
    {
        private double x;
        private double y;
        private double z;

        public Position(double x, double y, double z, float tickDelta)
        {
            super(tickDelta);
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }

    @Cancelable
    @Getter
    @Setter
    public static class Rotation extends CameraEvent
    {
        private float yaw;
        private float pitch;

        public Rotation(float yaw, float pitch, float tickDelta)
        {
            super(tickDelta);
            this.yaw = yaw;
            this.pitch = pitch;
        }
    }
}
