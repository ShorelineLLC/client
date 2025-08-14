package net.shoreline.client.impl.block;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.fluid.FluidState;

@RequiredArgsConstructor
@Getter
public class AsyncBlockState
{
    private final BlockState blockState;
    private final FluidState fluidState;
    private final BlockEntity blockEntity;

    public static AsyncBlockState getDefaultState()
    {
        BlockState state = Blocks.AIR.getDefaultState();
        return new AsyncBlockState(state, state.getFluidState(), null);
    }
}
