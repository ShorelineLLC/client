package net.shoreline.client.impl.world.explosion;

import lombok.experimental.UtilityClass;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.shoreline.client.util.world.RaytraceContext;

import java.util.Collections;
import java.util.Set;
import java.util.function.BiFunction;

@UtilityClass
public class ExplosionTrace
{
    public float getDamageToPos(final BlockView blockView,
                                final Vec3d source,
                                final Vec3d pos,
                                final Box box,
                                final float power,
                                final boolean ignoreTerrain)
    {
        return getDamageToPos(blockView, source, pos, box, power, ignoreTerrain, Collections.emptySet());
    }

    public float getDamageToPos(final BlockView blockView,
                                final Vec3d source,
                                final Vec3d pos,
                                final Box box,
                                final float power,
                                final boolean ignoreTerrain,
                                final Set<BlockPos> ignoreBlocks)
    {
        double d = Math.sqrt(pos.squaredDistanceTo(source));
        RaycastFactory raycastFactory = getRaycastFactory(blockView, ignoreTerrain, ignoreBlocks);
        double ab = getExposure(source, box, raycastFactory);
        double w = d / power;
        double ac = (1.0 - w) * ab;
        return (float) ((int) ((ac * ac + ac) / 2.0 * 7.0 * 12.0 + 1.0));
    }

    private float getExposure(final Vec3d source,
                              final Box box,
                              final RaycastFactory raycastFactory)
    {
        double xDiff = box.maxX - box.minX;
        double yDiff = box.maxY - box.minY;
        double zDiff = box.maxZ - box.minZ;

        double xStep = 1 / (xDiff * 2 + 1);
        double yStep = 1 / (yDiff * 2 + 1);
        double zStep = 1 / (zDiff * 2 + 1);

        if (xStep > 0 && yStep > 0 && zStep > 0)
        {
            int misses = 0;
            int hits = 0;

            double xOffset = (1 - Math.floor(1 / xStep) * xStep) * 0.5;
            double zOffset = (1 - Math.floor(1 / zStep) * zStep) * 0.5;

            xStep = xStep * xDiff;
            yStep = yStep * yDiff;
            zStep = zStep * zDiff;

            double startX = box.minX + xOffset;
            double startY = box.minY;
            double startZ = box.minZ + zOffset;
            double endX = box.maxX + xOffset;
            double endY = box.maxY;
            double endZ = box.maxZ + zOffset;

            for (double x = startX; x <= endX; x += xStep)
            {
                for (double y = startY; y <= endY; y += yStep)
                {
                    for (double z = startZ; z <= endZ; z += zStep)
                    {
                        Vec3d position = new Vec3d(x, y, z);

                        if (raycast(new RaytraceContext(position, source), raycastFactory) == null)
                        {
                            misses++;
                        }

                        hits++;
                    }
                }
            }

            return (float) misses / hits;
        }

        return 0f;
    }

    private RaycastFactory getRaycastFactory(final BlockView blockView,
                                             final boolean ignoreTerrain,
                                             final Set<BlockPos> ignoreBlocks)
    {
        return (context, blockPos) ->
        {
            if (ignoreBlocks.contains(blockPos))
            {
                return null;
            }

            BlockState blockState = blockView.getBlockState(blockPos);
            Block block = blockState.getBlock();

            if (ignoreTerrain && block.getBlastResistance() < 600)
            {
                return null;
            }

            VoxelShape voxelShape = blockState.getCollisionShape(blockView, blockPos);

            return voxelShape.raycast(context.start(), context.end(), blockPos);
        };
    }

    private BlockHitResult raycast(RaytraceContext context, RaycastFactory raycastFactory)
    {
        return BlockView.raycast(context.start(), context.end(), context, raycastFactory, ctx -> null);
    }

    @FunctionalInterface
    public interface RaycastFactory extends BiFunction<RaytraceContext, BlockPos, BlockHitResult> {}
}
