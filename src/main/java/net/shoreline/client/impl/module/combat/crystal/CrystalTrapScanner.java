package net.shoreline.client.impl.module.combat.crystal;

import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.shoreline.client.impl.mining.MiningData;
import net.shoreline.client.impl.module.combat.AutoCrystalModule;
import net.shoreline.client.impl.module.combat.trap.TrapLayer;
import net.shoreline.client.impl.module.combat.trap.TrapPositionCalc;
import net.shoreline.client.impl.module.combat.trap.TrapSpec;
import net.shoreline.client.impl.module.combat.util.AsyncDamageUtil;
import net.shoreline.client.impl.world.EntityState;
import net.shoreline.client.impl.world.LivingEntityState;

import java.util.EnumSet;
import java.util.Set;

public abstract class CrystalTrapScanner extends CrystalEntityScanner
{
    private final TrapPositionCalc trapPositionCalc = new TrapPositionCalc();

    private static final EntityDimensions ITEM_DIMENSIONS
            = EntityDimensions.fixed(0.25f, 0.25f);

    private static final TrapSpec FEET_TRAP_SPEC = TrapSpec.builder()
            .layers(EnumSet.of(TrapLayer.FEET))
            .extendBody(false)
            .extendFeet(false)
            .build();

    protected CrystalTrapScanner(AutoCrystalModule autoCrystal)
    {
        super(autoCrystal);
    }

    protected boolean isSurroundBreakPos(BlockPos blockPos,
                                         Vec3d crystalVec,
                                         PlayerEntity target,
                                         MiningData currentMine)
    {
        BlockPos minePos = currentMine.getBlockPos();
        LivingEntityState state = (LivingEntityState) getEntityById(target.getId());

        trapPositionCalc.calcTrap(state.getBoundingBox(), FEET_TRAP_SPEC);
        if (!trapPositionCalc.getTrapPositions().contains(minePos))
        {
            return false;
        }

        if (currentMine.isDoneMining())
        {
            float baseDamage = getAssumedDamage(minePos.toBottomCenterPos(), minePos, state);
            if (baseDamage < autoCrystal.getMinDamage().getValue())
            {
                return false;
            }

            for (EntityState entityState : getOtherEntities(null, new Box(minePos)))
            {
                if (entityState.getEntityType() != EntityType.ITEM)
                {
                    continue;
                }

                if (getExplosionDamage(crystalVec,
                        entityState.getPos(),
                        entityState.getBoundingBox(),
                        false,
                        Set.of(minePos)) >= 5.0f)
                {
                    return true;
                }
            }
        }

        else if (currentMine.isAlmostDone(autoCrystal.getPrePlace().getValue()))
        {
            Vec3d simPos = minePos.toBottomCenterPos();
            return getExplosionDamage(blockPos.up().toBottomCenterPos(),
                    simPos,
                    ITEM_DIMENSIONS.getBoxAt(simPos),
                    false,
                    Set.of(minePos)) >= 5.0f;
        }

        return false;
    }

    protected float getAssumedDamage(Vec3d crystalVec, BlockPos ignore, LivingEntityState state)
    {
        return AsyncDamageUtil.getAssumedDamage(getExplosionDamage(crystalVec,
                state.getPos(),
                state.getBoundingBox(),
                autoCrystal.getIgnoreTerrain().getValue(),
                Set.of(ignore)), state);
    }
}
