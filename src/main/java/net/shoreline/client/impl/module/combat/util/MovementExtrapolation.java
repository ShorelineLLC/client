package net.shoreline.client.impl.module.combat.util;

import lombok.experimental.UtilityClass;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.CollisionView;

@UtilityClass
public class MovementExtrapolation
{
    public Vec3d extrapolatePosition(CollisionView view,
                                     Vec3d velocity,
                                     Box box,
                                     Entity entity,
                                     int ticks)
    {
        return extrapolatePosition(view, velocity, box, entity, ticks, true);
    }


    public Vec3d extrapolatePosition(CollisionView view,
                                     Vec3d velocity,
                                     Box box,
                                     Entity entity,
                                     int ticks,
                                     boolean simulateY)
    {
        if (!simulateY)
        {
            velocity = velocity.multiply(1.0, 0.0, 1.0);
        }

        for (int i = 0; i < ticks; i++)
        {
            velocity = velocity.add(0.0, -0.08, 0.0).multiply(0.98, 0.98, 0.98);

            double dx = velocity.x;
            double dy = velocity.y;
            double dz = velocity.z;

            double collideX = collideAxis(view, entity, box, Direction.Axis.X, dx);
            box = box.offset(collideX, 0, 0);
            if (Math.abs(dx - collideX) > 1.0e-7)
            {
                velocity = new Vec3d(0.0, velocity.y, velocity.z);
            }

            double collideY = collideAxis(view, entity, box, Direction.Axis.Y, dy);
            box = box.offset(0, collideY, 0);
            boolean onGround = dy < 0.0 && Math.abs(dy - collideY) > 1.0E-7;
            if (Math.abs(dy - collideY) > 1.0e-7)
            {
                velocity = new Vec3d(velocity.x, 0.0, velocity.z);
            }

            double collideZ = collideAxis(view, entity, box, Direction.Axis.Z, dz);
            box = box.offset(0, 0, collideZ);
            if (Math.abs(dz - collideZ) > 1.0E-7)
            {
                velocity = new Vec3d(velocity.x, velocity.y, 0.0);
            }

            if (onGround)
            {
                double friction = 0.91;
                try {
                    BlockPos below = BlockPos.ofFloored(
                            (box.minX + box.maxX) * 0.5,
                            box.minY - 0.500001,
                            (box.minZ + box.maxZ) * 0.5
                    );

                    friction *= view.getBlockState(below).getBlock().getVelocityMultiplier();
                } catch (Throwable ignored) {}

                velocity = new Vec3d(velocity.x * friction, velocity.y, velocity.z * friction);
            }
        }

        double cx = (box.minX + box.maxX) * 0.5;
        double cz = (box.minZ + box.maxZ) * 0.5;
        return new Vec3d(cx, box.minY, cz);
    }

    /** Axis-aligned collision resolution against blocks. */
    private double collideAxis(CollisionView view,
                               Entity entity,
                               Box box,
                               Direction.Axis axis,
                               double target)
    {
        if (target == 0.0)
        {
            return 0.0;
        }

        Box collisionArea = box.stretch(
                axis == Direction.Axis.X ? target : 0.0,
                axis == Direction.Axis.Y ? target : 0.0,
                axis == Direction.Axis.Z ? target : 0.0
        ).expand(1.0e-7);

        Iterable<VoxelShape> shapes = view.getBlockCollisions(entity, collisionArea);
        return VoxelShapes.calculateMaxOffset(axis, box, shapes, target);
    }
}

