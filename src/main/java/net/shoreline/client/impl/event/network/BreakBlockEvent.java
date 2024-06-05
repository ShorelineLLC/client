package net.shoreline.client.impl.event.network;

import net.minecraft.util.math.BlockPos;
import net.shoreline.eventbus.annotation.Cancelable;
import net.shoreline.eventbus.event.Event;

@Cancelable
public class BreakBlockEvent extends Event
{
    private final BlockPos pos;

    public BreakBlockEvent(BlockPos pos)
    {
        this.pos = pos;
    }

    public BlockPos getPos()
    {
        return pos;
    }
}
