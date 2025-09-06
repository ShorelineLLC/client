package net.shoreline.client.impl.module.combat.crystal;

import lombok.experimental.UtilityClass;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.shoreline.client.impl.Managers;
import net.shoreline.client.impl.mining.MiningData;
import net.shoreline.client.impl.module.combat.AutoMineModule;
import net.shoreline.client.impl.module.world.SpeedMineModule;

@UtilityClass
public class AntiSurround
{
    public boolean checkAntiSurroundQualifiers(BlockPos blockPos)
    {
        if (!AutoMineModule.INSTANCE.isEnabled() || !SpeedMineModule.INSTANCE.isEnabled())
        {
            return false;
        }

        PlayerEntity target = Managers.TARGETING.getTarget();
        if (target == null)
        {
            return false;
        }

        MiningData currentMine = SpeedMineModule.INSTANCE.getMainMiningBlock();
        if (currentMine == null || currentMine.getProgress() < 0.7f || SpeedMineModule.INSTANCE.isManualMining())
        {
            return false;
        }

        for (Direction direction : Direction.values())
        {
            BlockPos pos1 = currentMine.getBlockPos().offset(direction);
            if (blockPos.equals(pos1))
            {
                return true;
            }
        }

        return false;
    }
}
