package net.shoreline.client.impl.interact;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

@Getter
@Setter
@Builder
public class Interaction
{
    private final Block block;
    private final BlockPos pos;
    private final Hand hand;

    private Direction direction;
    private final boolean packetPlace = true;

    public BlockState getState()
    {
        return MinecraftClient.getInstance().world.getBlockState(pos);
    }

    public Vec3d getHitVec()
    {
        return new Vec3d(direction.getUnitVector()).multiply(0.5);
    }
}
