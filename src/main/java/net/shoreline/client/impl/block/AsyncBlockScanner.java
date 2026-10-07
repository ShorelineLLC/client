package net.shoreline.client.impl.block;

import lombok.RequiredArgsConstructor;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;

import java.util.Map;

@RequiredArgsConstructor
public abstract class AsyncBlockScanner extends AsyncCollisionScanner
{
    private final BlockPos.Mutable mPos = new BlockPos.Mutable();

    private int height, bottomY;

    public void createCube(ClientWorld world, BlockPos center)
    {
        blockStates.clear();

        int radius = getRadius();

        height = center.getY() + radius;
        bottomY = center.getY() - radius;
        for (int dx = -radius; dx <= radius; ++dx)
        {
            for (int dy = -radius; dy <= radius; ++dy)
            {
                for (int dz = -radius; dz <= radius; ++dz)
                {
                    mPos.set(center.getX() + dx,
                            center.getY() + dy,
                            center.getZ() + dz);
                    BlockPos key = mPos.toImmutable();

                    AsyncBlockState blockState = new AsyncBlockState(world.getBlockState(key),
                            world.getFluidState(key), world.getBlockEntity(key));
                    blockStates.put(key, blockState);
                }
            }
        }
    }

    public void createSphere(ClientWorld world, BlockPos center)
    {
        blockStates.clear();

        final int r = getRadius();
        final int r2 = r * r;

        height = center.getY() + r;
        bottomY = center.getY() - r;
        for (int dx = -r; dx <= r; ++dx)
        {
            final int dx2 = dx * dx;
            for (int dy = -r; dy <= r; ++dy)
            {
                final int dxy2 = dx2 + dy * dy;
                for (int dz = -r; dz <= r; ++dz)
                {
                    if (dxy2 + dz * dz <= r2)
                    {
                        mPos.set(center.getX() + dx,
                                center.getY() + dy,
                                center.getZ() + dz);
                        BlockPos key = mPos.toImmutable();

                        AsyncBlockState blockState = new AsyncBlockState(world.getBlockState(key),
                                world.getFluidState(key), world.getBlockEntity(key));
                        blockStates.put(key, blockState);
                    }
                }
            }
        }
    }

    public void scanBlocks()
    {
        for (Map.Entry<BlockPos, AsyncBlockState> entry : blockStates.entrySet())
        {
            visit(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public int getHeight()
    {
        return height;
    }

    @Override
    public int getBottomY()
    {
        return bottomY;
    }

    protected abstract void visit(BlockPos pos, AsyncBlockState state);

    protected abstract int getRadius();
}
