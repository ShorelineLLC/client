package net.shoreline.client.impl.block;

import lombok.RequiredArgsConstructor;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@RequiredArgsConstructor
public abstract class AsyncBlockScanner
{
    private final BlockPos.Mutable mPos = new BlockPos.Mutable();

    private final ConcurrentMap<BlockPos, BlockState> blocks = new ConcurrentHashMap<>();

    public void createCube(ClientWorld world, BlockPos center)
    {
        blocks.clear();

        int radius = getRadius();
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

                    blocks.put(key, world.getBlockState(key));
                }
            }
        }
    }

    public void createSphere(ClientWorld world, BlockPos center)
    {
        blocks.clear();

        final int r  = getRadius();
        final int r2 = r * r;

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

                        blocks.put(key, world.getBlockState(key));
                    }
                }
            }
        }
    }

    public void scanBlocks()
    {
        for (Map.Entry<BlockPos, BlockState> entry : blocks.entrySet())
        {
            visit(entry.getKey(), entry.getValue());
        }
    }

    protected BlockState getBlockState(BlockPos blockPos)
    {
        return blocks.getOrDefault(blockPos, Blocks.AIR.getDefaultState());
    }

    protected abstract void visit(BlockPos pos, BlockState state);

    protected abstract int getRadius();
}
