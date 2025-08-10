package net.shoreline.client.impl.block;

import lombok.RequiredArgsConstructor;
import net.minecraft.block.BlockState;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;

@RequiredArgsConstructor
public abstract class BlockScanner
{
    private final BlockPos.Mutable mPos = new BlockPos.Mutable();

    public void scan(ClientWorld world, BlockPos center)
    {
        for (int dx = -getRadius(); dx <= getRadius(); ++dx)
        {
            for (int dy = -getRadius(); dy <= getRadius(); ++dy)
            {
                for (int dz = -getRadius(); dz <= getRadius(); ++dz)
                {
                    mPos.set(center.getX() + dx,
                            center.getY() + dy,
                            center.getZ() + dz);
                    visit(world, mPos, world.getBlockState(mPos));
                }
            }
        }
    }

    protected abstract void visit(ClientWorld world, BlockPos pos, BlockState state);

    protected abstract int getRadius();
}

