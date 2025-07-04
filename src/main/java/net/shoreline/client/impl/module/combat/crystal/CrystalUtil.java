package net.shoreline.client.impl.module.combat.crystal;

import lombok.experimental.UtilityClass;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

@UtilityClass
public class CrystalUtil
{
    public boolean canUseOnBlock(BlockPos pos, BlockState state)
    {
        return false;
    }
}
