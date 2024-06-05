package net.shoreline.client.impl.event.entity;

import net.minecraft.entity.LivingEntity;
import net.shoreline.eventbus.event.Event;

public class UpdateServerPositionEvent extends Event
{
    private final LivingEntity livingEntity;
    private final double x, y, z;
    private final float yaw, pitch;

    public UpdateServerPositionEvent(LivingEntity livingEntity, double x, double y, double z, float yaw, float pitch)
    {
        this.livingEntity = livingEntity;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public LivingEntity getLivingEntity()
    {
        return livingEntity;
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

    public float getYaw()
    {
        return yaw;
    }

    public float getPitch()
    {
        return pitch;
    }
}
