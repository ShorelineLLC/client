package net.shoreline.client.impl.event.render.block.entity;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.shoreline.eventbus.event.Event;

public class RenderBlockEntityEvent extends Event
{
    private final BlockEntity blockEntity;

    public RenderBlockEntityEvent(BlockEntity blockEntity)
    {
        this.blockEntity = blockEntity;
    }

    public BlockPos getPos()
    {
        return blockEntity.getPos();
    }

    public BlockEntity getBlockEntity()
    {
        return blockEntity;
    }

    public BlockState getCachedState()
    {
        return blockEntity.getCachedState();
    }
}
