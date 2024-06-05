package net.shoreline.client.impl.event.camera;

import net.minecraft.util.math.Vec3d;
import net.shoreline.eventbus.event.Event;

public class CameraPositionEvent extends Event
{

    private double x;
    private double y;
    private double z;
    private final float tickDelta;

    public CameraPositionEvent(double x, double y, double z, float tickDelta)
    {
        this.x = x;
        this.y = y;
        this.z = z;
        this.tickDelta = tickDelta;
    }

    public double getX()
    {
        return x;
    }

    public double getY()
    {
        return y;
    }

    public double getZ()
    {
        return z;
    }

    public float getTickDelta()
    {
        return tickDelta;
    }

    public void setX(double x)
    {
        this.x = x;
    }

    public void setY(double y)
    {
        this.y = y;
    }

    public void setZ(double z)
    {
        this.z = z;
    }

    public void setPosition(Vec3d pos)
    {
        this.x = pos.getX();
        this.y = pos.getY();
        this.z = pos.getZ();
    }

    public Vec3d getPosition()
    {
        return new Vec3d(x, y, z);
    }
}
