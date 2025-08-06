package net.shoreline.client.util.world;

import lombok.experimental.UtilityClass;
import net.minecraft.util.math.BlockPos;

@UtilityClass
public class PositionUtil
{
    public BlockPos[] getOffsets(BlockPos blockPos)
    {
        return new BlockPos[]
                {
                        blockPos.south(),
                        blockPos.south().west(),
                        blockPos.west(),
                        blockPos.north().west(),
                        blockPos.north(),
                        blockPos.north().east(),
                        blockPos.east(),
                        blockPos.south().east()
                };
    }
}
