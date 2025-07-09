package net.shoreline.client.impl.manager.interact;

import lombok.Builder;
import lombok.Getter;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

@Getter
@Builder
public class Interaction
{
    private final Block block;
    private final BlockPos pos;
    private final Direction direction;
    private final Hand hand;
    private final boolean packetPlace;

    public BlockState getState()
    {
        return MinecraftClient.getInstance().world.getBlockState(pos);
    }
}
