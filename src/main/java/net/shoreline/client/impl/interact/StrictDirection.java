package net.shoreline.client.impl.interact;

import lombok.experimental.UtilityClass;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.shoreline.client.impl.module.client.AnticheatModule;

@UtilityClass
public class StrictDirection
{
    private final AnticheatModule anticheat = AnticheatModule.INSTANCE;

    public Direction getInteractDirection(BlockPos blockPos)
    {
        Direction interactDirection = null;
        for (final Direction direction : Direction.values())
        {
            BlockState state = MinecraftClient.getInstance().world.getBlockState(blockPos.offset(direction));
            if (state.isAir() || !state.getFluidState().isEmpty())
            {
                continue;
            }

            if (state.getBlock() == Blocks.ANVIL
                    || state.getBlock() == Blocks.CHIPPED_ANVIL
                    || state.getBlock() == Blocks.DAMAGED_ANVIL)
            {
                continue;
            }

            interactDirection = direction.getOpposite();
            break;
        }

        return interactDirection;
    }
}
