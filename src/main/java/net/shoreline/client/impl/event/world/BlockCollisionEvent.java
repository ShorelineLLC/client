package net.shoreline.client.impl.event.world;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.shoreline.eventbus.annotation.Cancelable;
import net.shoreline.eventbus.event.Event;

/**
 * @author linus
 * @since 1.0
 */
@Cancelable
public class BlockCollisionEvent extends Event
{
    //
    private final BlockPos pos;
    private final BlockState state;
    //
    private VoxelShape voxelShape;

    public BlockCollisionEvent(VoxelShape voxelShape, BlockPos pos, BlockState state)
    {
        this.pos = pos;
        this.state = state;
        this.voxelShape = voxelShape;
    }

    public BlockPos getPos()
    {
        return pos;
    }

    public BlockState getState()
    {
        return state;
    }

    public Block getBlock()
    {
        return state.getBlock();
    }

    public VoxelShape getVoxelShape()
    {
        return voxelShape;
    }

    public void setVoxelShape(VoxelShape voxelShape)
    {
        this.voxelShape = voxelShape;
    }
}
