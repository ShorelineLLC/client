package net.shoreline.client.impl.block;

import lombok.RequiredArgsConstructor;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.CollisionView;
import net.shoreline.client.impl.world.LivingEntityState;

@RequiredArgsConstructor
public class AsyncShapeContext implements ShapeContext
{
    private final LivingEntityState entityState;

    @Override
    public boolean isDescending()
    {
        return entityState.isDescending();
    }

    @Override
    public boolean isAbove(VoxelShape shape, BlockPos pos, boolean defaultValue)
    {
        return entityState.getY() > pos.getY() + shape.getMax(Direction.Axis.Y) - 1.0E-5f;
    }

    @Override
    public boolean isHolding(Item item)
    {
        return entityState.getHeldItem().equals(item);
    }

    @Override
    public boolean canWalkOnFluid(FluidState stateAbove, FluidState state)
    {
        return false;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, CollisionView world, BlockPos pos)
    {
        return state.getCollisionShape(world, pos, this);
    }
}
