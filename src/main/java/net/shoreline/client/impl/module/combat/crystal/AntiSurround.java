package net.shoreline.client.impl.module.combat.crystal;

import lombok.experimental.UtilityClass;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.shoreline.client.impl.Managers;
import net.shoreline.client.impl.mining.MiningData;
import net.shoreline.client.impl.module.combat.AutoCrystalModule;
import net.shoreline.client.impl.module.combat.AutoMineModule;
import net.shoreline.client.impl.module.world.SpeedMineModule;
import net.shoreline.client.impl.world.explosion.ExplosionUtil;

import java.util.Set;

@UtilityClass
public class AntiSurround
{

    public boolean checkAntiSurroundQualifiers(BlockView blockView, BlockPos blockPos)
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
        if (currentMine == null || !currentMine.isDoneMining() || SpeedMineModule.INSTANCE.isManualMining())
        {
            return false;
        }

        BlockPos minePos = currentMine.getBlockPos();
        double damage = ExplosionUtil.crystalDamageToEntity(blockView,
                target,
                minePos.toBottomCenterPos(),
                AutoCrystalModule.INSTANCE.getIgnoreTerrain().getValue(),
                Set.of(minePos));

        if (damage < AutoCrystalModule.INSTANCE.getMinDamage().getValue())
        {
            return false;
        }

        for (Direction direction : Direction.values())
        {
            BlockPos pos = minePos.offset(direction);
            if (!AutoCrystalModule.INSTANCE.canUseOnBlock(pos.down()))
            {
                continue;
            }
            
            if (blockPos.equals(pos))
            {
                return true;
            }
        }

        return false;
    }
}
