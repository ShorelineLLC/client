package net.shoreline.client.impl.module.helper;

import lombok.RequiredArgsConstructor;
import net.minecraft.block.BlockState;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;

@RequiredArgsConstructor
public abstract class BlockScanner
{
    protected final int radius;
    private final BlockPos.Mutable mPos = new BlockPos.Mutable();

    public final void scan(ClientWorld world, BlockPos center)
    {
        for (int dx = -radius; dx <= radius; ++dx)
        {
            for (int dy = -radius; dy <= radius; ++dy)
            {
                for (int dz = -radius; dz <= radius; ++dz)
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
}

