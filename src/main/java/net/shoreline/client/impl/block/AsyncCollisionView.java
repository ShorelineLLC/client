package net.shoreline.client.impl.block;

import net.minecraft.util.math.Box;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.CollisionView;
import net.shoreline.client.impl.world.LivingEntityState;

public interface AsyncCollisionView extends CollisionView
{
    Iterable<VoxelShape> getBlockCollisions(LivingEntityState entity, Box box);
}
