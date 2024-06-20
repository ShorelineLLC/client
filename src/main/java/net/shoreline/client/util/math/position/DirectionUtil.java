package net.shoreline.client.util.math.position;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class DirectionUtil
{
    public static Vec3d getDirectionOffsetPos(BlockPos pos, Direction direction)
    {
        Vec3d pos1 = pos.toCenterPos();
        return switch (direction)
        {
            case UP -> pos1.add(0.0, 0.5, 0.0);
            case DOWN -> pos1.add(0.0, -0.5, 0.0);
            case NORTH -> pos1.add(0.0, 0.0, -0.5);
            case SOUTH -> pos1.add(0.0, 0.0, 0.5);
            case WEST -> pos1.add(-0.5, 0.0, 0.0);
            case EAST -> pos1.add(0.5, 0.0, 0.0);
        };
    }
}
