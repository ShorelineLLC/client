package net.shoreline.client.api.render;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.shoreline.client.util.Globals;


/**
 * @author linus
 * @since 1.0
 */
public class Interpolation implements Globals
{
    public static Vec3d getRenderPosition(Vec3d pos, Vec3d lastPos, float tickDelta)
    {
        return new Vec3d(pos.x - MathHelper.lerp(tickDelta, lastPos.x, pos.x),
                pos.y - MathHelper.lerp(tickDelta, lastPos.y, pos.y),
                pos.z - MathHelper.lerp(tickDelta, lastPos.z, pos.z));
    }

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
        return new Vec3d(entity.getX() - MathHelper.lerp(tickDelta, entity.lastRenderX, entity.getX()),
                entity.getY() - MathHelper.lerp(tickDelta, entity.lastRenderY, entity.getY()),
                entity.getZ() - MathHelper.lerp(tickDelta, entity.lastRenderZ, entity.getZ()));
    }

    /**
     * @param entity
     * @param tickDelta
     * @return
     */
    public static Vec3d getInterpolatedPosition(Entity entity, float tickDelta)
    {
        return new Vec3d(entity.prevX + ((entity.getX() - entity.prevX) * tickDelta),
                entity.prevY + ((entity.getY() - entity.prevY) * tickDelta),
                entity.prevZ + ((entity.getZ() - entity.prevZ) * tickDelta));
    }

    /**
     * @param prev
     * @param value
     * @param factor
     * @return
     */
    public static float interpolateFloat(float prev, float value, float factor)
    {
        return prev + ((value - prev) * factor);
    }

    /**
     * @param prev
     * @param value
     * @param factor
     * @return
     */
    public static double interpolateDouble(double prev, double value, double factor)
    {
        return prev + ((value - prev) * factor);
    }

    /**
     * @param prevBox
     * @param box
     * @return
     */
    public static Box getInterpolatedBox(Box prevBox, Box box)
    {

        double delta = mc.isPaused() ? 1f : mc.getRenderTickCounter().getTickDelta(true);

        return new Box(interpolateDouble(prevBox.minX, box.minX, delta),
                interpolateDouble(prevBox.minY, box.minY, delta),
                interpolateDouble(prevBox.minZ, box.minZ, delta),
                interpolateDouble(prevBox.maxX, box.maxX, delta),
                interpolateDouble(prevBox.maxY, box.maxY, delta),
                interpolateDouble(prevBox.maxZ, box.maxZ, delta));
    }

    /**
     * @param entity
     * @return
     */
    public static Box getInterpolatedEntityBox(Entity entity)
    {
        Box box = entity.getBoundingBox();
        Box prevBox = entity.getBoundingBox().offset(entity.prevX - entity.getX(), entity.prevY - entity.getY(), entity.prevZ - entity.getZ());
        return getInterpolatedBox(prevBox, box);
    }
}
