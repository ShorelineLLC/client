package net.shoreline.client.impl.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public abstract class AsyncBlockView implements BlockView
{
    protected final ConcurrentMap<BlockPos, BlockState> blockStates = new ConcurrentHashMap<>();
    protected final ConcurrentMap<BlockPos, FluidState> fluidStates = new ConcurrentHashMap<>();

    @Override
    public BlockState getBlockState(BlockPos blockPos)
    {
        return blockStates.getOrDefault(blockPos, Blocks.AIR.getDefaultState());
    }

    @Override
    public FluidState getFluidState(BlockPos pos)
    {
        return fluidStates.getOrDefault(pos, Blocks.AIR.getDefaultState().getFluidState());
    }

    @Override
    public BlockEntity getBlockEntity(BlockPos pos)
    {
        return null;
    }
}
