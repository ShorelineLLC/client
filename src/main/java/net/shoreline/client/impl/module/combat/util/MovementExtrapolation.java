package net.shoreline.client.impl.module.combat.util;

import lombok.experimental.UtilityClass;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.CollisionView;

import java.util.function.Function;

@UtilityClass
public class MovementExtrapolation
{
    public Vec3d extrapolatePosition(CollisionView collisionView,
                                     Function<Box, Iterable<VoxelShape>> blockCollisionFunction,
                                     Vec3d velocity,
                                     Box box,
                                     int ticks)
    {
        return extrapolatePosition(collisionView, blockCollisionFunction, velocity, box, ticks, true);
    }

    public Vec3d extrapolatePosition(CollisionView collisionView,
                                     Function<Box, Iterable<VoxelShape>> blockCollisionFunction,
                                     Vec3d velocity,
                                     Box box,
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

            Iterable<VoxelShape> collisionsX = blockCollisionFunction.apply(getCollisionBox(box, Direction.Axis.X, dx));
            double collideX = dx == 0.0 ? 0.0 : VoxelShapes.calculateMaxOffset(Direction.Axis.X, box, collisionsX, dx);
            box = box.offset(collideX, 0, 0);
            if (Math.abs(dx - collideX) > 1.0e-7)
            {
                velocity = new Vec3d(0.0, velocity.y, velocity.z);
            }

            Iterable<VoxelShape> collisionsY = blockCollisionFunction.apply(getCollisionBox(box, Direction.Axis.Y, dy));
            double collideY = dy == 0.0 ? 0.0 : VoxelShapes.calculateMaxOffset(Direction.Axis.Y, box, collisionsY, dy);
            box = box.offset(0, collideY, 0);
            boolean onGround = dy < 0.0 && Math.abs(dy - collideY) > 1.0E-7;
            if (Math.abs(dy - collideY) > 1.0e-7)
            {
                velocity = new Vec3d(velocity.x, 0.0, velocity.z);
            }

            Iterable<VoxelShape> collisionsZ = blockCollisionFunction.apply(getCollisionBox(box, Direction.Axis.Z, dz));
            double collideZ = dz == 0.0 ? 0.0 : VoxelShapes.calculateMaxOffset(Direction.Axis.Z, box, collisionsZ, dz);
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

                    friction *= collisionView.getBlockState(below).getBlock().getVelocityMultiplier();
                } catch (Throwable ignored) {}

                velocity = new Vec3d(velocity.x * friction, velocity.y, velocity.z * friction);
            }
        }

        double cx = (box.minX + box.maxX) * 0.5;
        double cz = (box.minZ + box.maxZ) * 0.5;
        return new Vec3d(cx, box.minY, cz);
    }

    private Box getCollisionBox(Box box, Direction.Axis axis, double target)
    {
        return box.stretch(
                axis == Direction.Axis.X ? target : 0.0,
                axis == Direction.Axis.Y ? target : 0.0,
                axis == Direction.Axis.Z ? target : 0.0
        ).expand(1.0e-7);
    }
}

