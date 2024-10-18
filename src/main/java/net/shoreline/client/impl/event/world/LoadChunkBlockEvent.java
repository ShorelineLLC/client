package net.shoreline.client.impl.event.world;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.shoreline.eventbus.event.Event;

public class LoadChunkBlockEvent extends Event
{
    private final BlockPos pos;
    private final BlockState state;

    public LoadChunkBlockEvent(BlockPos pos, BlockState state)
    {
        this.pos = pos;
        this.state = state;
    }

    public BlockPos getPos()
    {
        return pos;
    }

    public BlockState getState()
    {
        return state;
    }
}
