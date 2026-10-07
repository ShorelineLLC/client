package net.shoreline.client.impl.module.combat.crystal;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.shoreline.client.impl.mining.MiningData;
import net.shoreline.client.impl.module.combat.AutoCrystalModule;
import net.shoreline.client.util.entity.EntityUtil;

public abstract class CrystalCevScanner extends CrystalTrapScanner
{
    private boolean startedCevSequence;

    protected CrystalCevScanner(AutoCrystalModule autoCrystal)
    {
        super(autoCrystal);
    }

    protected boolean isCevBreakerPos(BlockPos blockPos,
                                    PlayerEntity target,
                                    MiningData currentMine)
    {
//        BlockPos targetHeadPos = EntityUtil.getRoundedBlockPos(target).up(target.isCrawling() ? 1 : 2);
//        if (!blockPos.down().equals(targetHeadPos) || !currentMine.getBlockPos().equals(targetHeadPos))
//        {
//            return false;
//        }

//        if (startedCevSequence)
//        {
//            if (!currentMine.isDoneMining())
//            {
//                return false;
//            }
//
//            startedCevSequence = false;
//            return true;
//        }
//
//        else if (currentMine.isAlmostDone(5))
//        {
//            return startedCevSequence = true;
//        }

        return false;
    }
}
