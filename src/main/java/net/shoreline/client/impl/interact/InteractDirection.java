package net.shoreline.client.impl.interact;

import lombok.experimental.UtilityClass;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.shoreline.client.impl.module.client.InteractionsModule;

@UtilityClass
public class InteractDirection
{
    private final InteractionsModule interactConfig = InteractionsModule.INSTANCE;

    public Direction getInteractDirection(BlockPos blockPos)
    {
        return getInteractDirection(blockPos, interactConfig.getStrictDirection().getValue());
    }

    public Direction getInteractDirection(BlockPos blockPos, boolean strictDir)
    {
        Direction interactDirection = null;
        for (final Direction direction : Direction.values())
        {
            Direction opposite = direction.getOpposite();
            if (strictDir && isDirectionHidden(blockPos, opposite))
            {
                continue;
            }

            BlockState state = MinecraftClient.getInstance().world.getBlockState(blockPos.offset(direction));
            if (state.isAir() || !state.getFluidState().isEmpty())
            {
                continue;
            }

            if (state.isOf(Blocks.ANVIL) || state.isOf(Blocks.CHIPPED_ANVIL) || state.isOf(Blocks.DAMAGED_ANVIL))
            {
                continue;
            }

            interactDirection = opposite;
            break;
        }

        return interactDirection;
    }

    public boolean isDirectionHidden(BlockPos blockPos, Direction direction)
    {
        PlayerEntity player = MinecraftClient.getInstance().player;
        double x = player.getX();
        double y = player.getEyeY();
        double z = player.getZ();

        Box blockBox = new Box(blockPos);
        if (blockBox.contains(x, y, z))
        {
            return false;
        }

        return switch (direction)
        {
            case NORTH -> z > blockBox.minZ; // Z- face
            case SOUTH -> z < blockBox.maxZ; // Z+ face
            case EAST  -> x < blockBox.maxX; // X+ face
            case WEST  -> x > blockBox.minX; // X- face
            case DOWN  -> y > blockBox.minY; // Y- face
            case UP -> false;
        };
    }
}
