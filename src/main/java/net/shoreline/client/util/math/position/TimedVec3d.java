package net.shoreline.client.util.math.position;

import net.minecraft.util.math.Position;
import net.minecraft.util.math.Vec3d;

/**
 * @author linus
 * @since 1.0
 */
public record TimedVec3d(Vec3d pos, long time) implements Position
{
    /**
     * @param pos
     * @param time
     */
    public TimedVec3d
    {

    }

    /**
     * Returns the X coordinate.
     */
    @Override
    public double getX()
    {
        return pos.getX();
    }

    /**
     * Returns the Y coordinate.
     */
    @Override
    public double getY()
    {
        return pos.getY();
    }

    /**
     * Returns the Z coordinate.
     */
    @Override
    public double getZ()
    {
        return pos.getZ();
    }
}
