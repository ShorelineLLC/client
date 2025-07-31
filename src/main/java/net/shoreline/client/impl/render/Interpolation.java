package net.shoreline.client.impl.render;

import lombok.experimental.UtilityClass;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

@UtilityClass
public class Interpolation
{
    /**
     * Gets the interpolated {@link Vec3d} position of an entity (i.e. position
     * based on render ticks)
     *
     * @param entity    The entity to get the position for
     * @param tickDelta The render time
     * @return The interpolated vector of an entity
     */
    public static Vec3d getRenderPosition(Entity entity, float tickDelta)
    {
        return new Vec3d(MathHelper.lerp(tickDelta, entity.lastRenderX, entity.getX()),
                MathHelper.lerp(tickDelta, entity.lastRenderY, entity.getY()),
                MathHelper.lerp(tickDelta, entity.lastRenderZ, entity.getZ()));
    }

    public Vec3d getRenderPosition(Vec3d pos, Vec3d lastPos, float tickDelta)
    {
        return new Vec3d(pos.x - MathHelper.lerp(tickDelta, lastPos.x, pos.x),
                pos.y - MathHelper.lerp(tickDelta, lastPos.y, pos.y),
                pos.z - MathHelper.lerp(tickDelta, lastPos.z, pos.z));
    }
}
