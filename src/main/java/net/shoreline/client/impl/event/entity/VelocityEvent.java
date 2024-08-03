package net.shoreline.client.impl.event.entity;

import net.minecraft.util.math.Vec3d;
import net.shoreline.eventbus.annotation.Cancelable;
import net.shoreline.eventbus.event.Event;

@Cancelable
public class VelocityEvent extends Event
{
    private Vec3d velocity;

    public VelocityEvent(Vec3d velocity)
    {
        this.velocity = velocity;
    }

    public void setVelocity(Vec3d velocity)
    {
        this.velocity = velocity;
    }

    public void setXYZ(double x, double y, double z)
    {
        this.velocity = new Vec3d(x, y, z);
    }

    public Vec3d getVelocity()
    {
        return velocity;
    }
}
