package net.shoreline.client.util.world;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

public record RaytraceContext(Vec3d start, Vec3d end)
{
    public RaycastContext getContextFromEntity(Entity entity)
    {
        return new RaycastContext(
                start,
                end,
                RaycastContext.ShapeType.COLLIDER,
                RaycastContext.FluidHandling.NONE,
                entity);
    }
}
