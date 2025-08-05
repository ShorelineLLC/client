package net.shoreline.client.impl.async;

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
    private final ConcurrentMap<BlockPos, BlockState> blockSphere = new ConcurrentHashMap<>();

    public void createSphere(ClientWorld world, BlockPos center)
    {
        blockSphere.clear();

        int radius = getRadius();
        for (int dx = -radius; dx <= radius; ++dx)
        {
            for (int dy = -radius; dy <= radius; ++dy)
            {
                for (int dz = -radius; dz <= radius; ++dz)
                {
                    BlockPos pos = BlockPos.ofFloored(center.getX() + dx,
                            center.getY() + dy,
                            center.getZ() + dz);

                    blockSphere.put(pos, world.getBlockState(pos));
                }
            }
        }
    }

    public void scanSphere()
    {
        for (Map.Entry<BlockPos, BlockState> entry : blockSphere.entrySet())
        {
            visit(entry.getKey(), entry.getValue());
        }
    }

    protected BlockState getBlockState(BlockPos blockPos)
    {
        return blockSphere.getOrDefault(blockPos, Blocks.AIR.getDefaultState());
    }

    protected abstract void visit(BlockPos pos, BlockState state);

    protected abstract int getRadius();
}
