package net.shoreline.client.impl.event.world;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.shoreline.eventbus.Event;
import net.shoreline.eventbus.annotation.Cancelable;

@AllArgsConstructor
@Getter
@Setter
@Cancelable
public class BlockCollisionEvent extends Event
{
    private VoxelShape collisionShape;

    private final BlockState state;
    private final BlockPos blockPos;
}
