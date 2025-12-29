package net.shoreline.client.impl.module.combat.crystal;

import lombok.RequiredArgsConstructor;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.shoreline.client.impl.Managers;
import net.shoreline.client.impl.mining.MiningData;
import net.shoreline.client.impl.module.combat.AutoCrystalModule;
import net.shoreline.client.impl.module.world.SpeedMineModule;
import net.shoreline.client.impl.world.EntityState;
import net.shoreline.client.impl.world.LivingEntityState;
import net.shoreline.client.impl.world.explosion.ExplosionUtil;
import net.shoreline.client.util.entity.EntityUtil;
import net.shoreline.client.util.item.ItemUtil;

import java.util.Set;

@RequiredArgsConstructor
public class CrystalDataFactory
{
    private final AutoCrystalModule autoCrystal;
    private final CrystalEntityScanner view;

    private boolean startedCevSequence;

    private static final EntityDimensions ITEM_DIMENSIONS = EntityDimensions.fixed(0.25f, 0.25f);

    private static final float ASSUMED_ARMOR_REDUCTION = 0.11f;

    public <T> CrystalData<T> createData(T value,
                                         Vec3d crystalVec,
                                         LivingEntityState target,
                                         float damageToTarget,
                                         float damageToPlayer)
    {
        BlockPos blockPos = BlockPos.ofFloored(crystalVec);

        if (autoCrystal.getOverrideConfig().getValue()
                && (isLethalCrystal(target, damageToTarget)
                || isArmorBreaker(target, damageToTarget)))
        {
            return new CrystalData.Immediate<>(value,
                    crystalVec,
                    target,
                    damageToTarget,
                    damageToPlayer);

        } else if (SpeedMineModule.INSTANCE.isUsedByAutoMine())
        {
            PlayerEntity mineTarget = Managers.TARGETING.getTarget();
            MiningData currentMine = SpeedMineModule.INSTANCE.getMainMiningBlock();
            if (mineTarget != null && currentMine != null)
            {
                if (autoCrystal.getTargetItems().getValue() && isAntiSurroundPos(blockPos, crystalVec, mineTarget, currentMine))
                {
                    return new CrystalData.Immediate<>("AS",
                            value,
                            crystalVec,
                            target,
                            damageToTarget,
                            damageToPlayer);
                }

                else if (autoCrystal.getCevBreak().getValue() && isCevBreakerPos(blockPos, mineTarget, currentMine))
                {
                    return new CrystalData.Immediate<>("Cev",
                            value,
                            crystalVec,
                            target,
                            damageToTarget,
                            damageToPlayer);
                }
            }
        }

        return new CrystalData<>(value,
                crystalVec,
                target,
                damageToTarget,
                damageToPlayer);
    }

    private boolean isLethalCrystal(LivingEntityState target, float damageToTarget)
    {
        return target.getTotalHealth() - (getAssumedDamage(damageToTarget, target) * autoCrystal.getDamageMultiplier().getValue()) < 0.0f;
    }

    private boolean isArmorBreaker(LivingEntityState target, float damage)
    {
        for (ItemStack armorStack : target.getArmorItems())
        {
            int armorDamage = ExplosionUtil.getArmorDurabilityDamage(armorStack, damage);
            float durability = ItemUtil.getDurability(armorStack) - (armorDamage * autoCrystal.getArmorMultiplier().getValue());
            if (durability <= 0)
            {
                return true;
            }
        }

        return false;
    }

    public boolean isAntiSurroundPos(BlockPos blockPos,
                                     Vec3d crystalVec,
                                     PlayerEntity target,
                                     MiningData currentMine)
    {
        BlockPos minePos = currentMine.getBlockPos();
        LivingEntityState state = (LivingEntityState) view.getEntityById(target.getId());

        if (!autoCrystal.getFeetTrap(state.getBoundingBox()).contains(minePos))
        {
            return false;
        }

        if (currentMine.isDoneMining())
        {
            float baseDamage = getAssumedDamage(minePos.toBottomCenterPos(), Set.of(minePos), state);
            if (baseDamage < autoCrystal.getMinDamage().getValue())
            {
                return false;
            }

            for (EntityState entityState : view.getOtherEntities(null, new Box(minePos)))
            {
                if (entityState.getEntityType() != EntityType.ITEM)
                {
                    continue;
                }

                if (view.getExplosionDamage(crystalVec,
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
            return view.getExplosionDamage(blockPos.toBottomCenterPos(),
                    simPos,
                    ITEM_DIMENSIONS.getBoxAt(simPos),
                    false,
                    Set.of(minePos)) >= 5.0f;
        }

        return false;
    }

    private boolean isCevBreakerPos(BlockPos blockPos,
                                    PlayerEntity target,
                                    MiningData currentMine)
    {
        BlockPos targetHeadPos = EntityUtil.getRoundedBlockPos(target).up(target.isCrawling() ? 1 : 2);
        if (!blockPos.down().equals(targetHeadPos) || !currentMine.getBlockPos().equals(targetHeadPos))
        {
            return false;
        }

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

    private float getAssumedDamage(Vec3d crystalVec, Set<BlockPos> ignore, LivingEntityState state)
    {
        return getAssumedDamage(view.getExplosionDamage(crystalVec,
                        state.getPos(),
                        state.getBoundingBox(),
                        autoCrystal.getIgnoreTerrain().getValue(),
                        ignore), state);
    }

    private float getAssumedDamage(float baseDamage, LivingEntityState state)
    {
        return state.getTotalArmor() > 0 ? baseDamage * ASSUMED_ARMOR_REDUCTION : baseDamage;
    }
}
