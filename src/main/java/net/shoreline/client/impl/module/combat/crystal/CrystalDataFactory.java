package net.shoreline.client.impl.module.combat.crystal;

import lombok.RequiredArgsConstructor;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.shoreline.client.impl.Managers;
import net.shoreline.client.impl.mining.MiningData;
import net.shoreline.client.impl.module.combat.AutoCrystalModule;
import net.shoreline.client.impl.module.combat.AutoMineModule;
import net.shoreline.client.impl.module.world.SpeedMineModule;
import net.shoreline.client.impl.world.EntityState;
import net.shoreline.client.impl.world.LivingEntityState;
import net.shoreline.client.impl.world.explosion.ExplosionUtil;
import net.shoreline.client.util.item.ItemUtil;

import java.util.Set;

@RequiredArgsConstructor
public class CrystalDataFactory
{
    private final AutoCrystalModule autoCrystal;
    private final CrystalEntityScanner view;

    private static final EntityDimensions ITEM_DIMENSIONS = EntityDimensions.fixed(0.25f, 0.25f);

    private static final float ASSUMED_ARMOR_REDUCTION = 0.11f;

    public <T> CrystalData<T> createData(T value,
                                         Vec3d crystalVec,
                                         LivingEntityState target,
                                         float damageToTarget,
                                         float damageToPlayer)
    {
        BlockPos blockPos = BlockPos.ofFloored(crystalVec);

        float appliedDamage = damageToTarget;
        if (target.getTotalArmor() > 0)
        {
            appliedDamage *= ASSUMED_ARMOR_REDUCTION; // We have to assume armor here...
        }

        if (autoCrystal.getOverrideConfig().getValue() && (isLethalCrystal(target, appliedDamage) || isArmorBreaker(target, damageToTarget)))
        {
            return new CrystalData.Immediate<>(value, crystalVec, target, damageToTarget, damageToPlayer);
        } else if (autoCrystal.getTargetItems().getValue() && isAntiSurroundPos(blockPos))
        {
            return new CrystalData.Immediate<>("AS", value, crystalVec, target, damageToTarget, damageToPlayer);
        } else
        {
            return new CrystalData<>(value, crystalVec, target, damageToTarget, damageToPlayer);
        }
    }

    private boolean isLethalCrystal(LivingEntityState target, float damageToTarget)
    {
        return target.getTotalHealth() - (damageToTarget * autoCrystal.getDamageMultiplier().getValue()) < 0.0f;
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

    public boolean isAntiSurroundPos(BlockPos blockPos)
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
        if (currentMine == null || SpeedMineModule.INSTANCE.isManualMining())
        {
            return false;
        }

        if (currentMine.isDoneMining())
        {
            BlockPos minePos = currentMine.getBlockPos();
            LivingEntityState state = (LivingEntityState) view.getEntityById(target.getId());

            float baseDamage = view.getExplosionDamage(minePos.toBottomCenterPos(),
                    state.getPos(),
                    state.getBoundingBox(),
                    autoCrystal.getIgnoreTerrain().getValue(),
                    Set.of(minePos));

            if (state.getTotalArmor() > 0)
            {
                baseDamage *= ASSUMED_ARMOR_REDUCTION;
            }

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

                float damage = view.getExplosionDamage(blockPos.toBottomCenterPos(),
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

        else if (currentMine.isAlmostDone(autoCrystal.getPrePlace().getValue()))
        {
            BlockPos minePos = currentMine.getBlockPos();
            Vec3d simPos = minePos.toBottomCenterPos();
            float damage = view.getExplosionDamage(blockPos.toBottomCenterPos(),
                    simPos,
                    ITEM_DIMENSIONS.getBoxAt(simPos),
                    false,
                    Set.of(minePos));

            return damage >= 5.0f;
        }

        return false;
    }
}
