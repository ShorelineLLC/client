package net.shoreline.client.impl.module.combat.crystal;

import lombok.experimental.UtilityClass;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.shoreline.client.impl.Managers;
import net.shoreline.client.impl.mining.MiningData;
import net.shoreline.client.impl.module.combat.AutoCrystalModule;
import net.shoreline.client.impl.module.combat.AutoMineModule;
import net.shoreline.client.impl.module.world.SpeedMineModule;
import net.shoreline.client.impl.world.AsyncWorldScanner;
import net.shoreline.client.impl.world.EntityState;

import java.util.Set;

@UtilityClass
public class AntiSurround
{
    public boolean checkAntiSurroundQualifiers(AsyncWorldScanner view, BlockPos blockPos)
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
        if (minePos.equals(blockPos))
        {
            return false;
        }

        EntityState state = view.getEntityById(target.getId());

        float baseDamage = CrystalUtil.getCrystalDamage(view,
                minePos.toBottomCenterPos(),
                state.getPos(),
                state.getBoundingBox(),
                AutoCrystalModule.INSTANCE.getIgnoreTerrain().getValue(),
                Set.of(minePos));

        baseDamage *= 0.11f; // We have to assume armor here...
        if (baseDamage < AutoCrystalModule.INSTANCE.getMinDamage().getValue())
        {
            return false;
        }

        for (EntityState entityState : view.getOtherEntities(null, new Box(minePos)))
        {
            if (entityState.getEntity() instanceof ItemEntity)
            {
                float damage = CrystalUtil.getCrystalDamage(view,
                        blockPos.toBottomCenterPos(),
                        entityState.getPos(),
                        entityState.getBoundingBox(),
                        false,
                        Set.of(minePos));

                if (damage >= 5.0f)
                {
                    return true;
                }
            }
        }

        return false;
    }
}
