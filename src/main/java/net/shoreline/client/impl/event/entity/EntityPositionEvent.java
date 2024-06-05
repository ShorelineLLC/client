package net.shoreline.client.impl.event.entity;

import net.minecraft.util.math.Vec3d;
import net.shoreline.eventbus.event.Event;

public class EntityPositionEvent extends Event
{
    private final Vec3d updatePos;
    private final Vec3d prevPos;

    public EntityPositionEvent(Vec3d updatePos, Vec3d prevPos)
    {
        this.updatePos = updatePos;
        this.prevPos = prevPos;
    }

    public Vec3d getUpdatePos()
    {
        return updatePos;
    }

    public Vec3d getPrevPos()
    {
        return prevPos;
    }
}
