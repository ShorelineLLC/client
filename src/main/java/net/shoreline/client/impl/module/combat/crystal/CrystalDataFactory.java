package net.shoreline.client.impl.module.combat.crystal;

import lombok.RequiredArgsConstructor;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.shoreline.client.impl.mining.MiningData;
import net.shoreline.client.impl.module.combat.AutoCrystalModule;
import net.shoreline.client.impl.module.combat.util.AsyncDamageUtil;
import net.shoreline.client.impl.world.LivingEntityState;
import net.shoreline.client.impl.world.explosion.ExplosionUtil;
import net.shoreline.client.util.entity.EntityUtil;
import net.shoreline.client.util.item.ItemUtil;

import java.util.function.Supplier;

@RequiredArgsConstructor
public class CrystalDataFactory
{
    private final AutoCrystalModule autoCrystal;

    public <T> CrystalData<T> createData(T value,
                                         Vec3d crystalVec,
                                         LivingEntityState target,
                                         float damageToTarget,
                                         float damageToPlayer)
    {
        return createData(value, crystalVec, target, damageToTarget, damageToPlayer, null);
    }

    public <T> CrystalData<T> createData(T value,
                                         Vec3d crystalVec,
                                         LivingEntityState target,
                                         float damageToTarget,
                                         float damageToPlayer,
                                         Supplier<String> immediateTag)
    {
        if (autoCrystal.getOverrideConfig().getValue()
                && (isLethalCrystal(target, damageToTarget)
                || isArmorBreaker(target, damageToTarget)))
        {
            return new CrystalData.Immediate<>(value,
                    crystalVec,
                    target,
                    damageToTarget,
                    damageToPlayer);
        }

        if (immediateTag != null)
        {
            String tagText = immediateTag.get();
            if (tagText != null)
            {
                return new CrystalData.Immediate<>(tagText,
                        value,
                        crystalVec,
                        target,
                        damageToTarget,
                        damageToPlayer);
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
        return target.getTotalHealth() - (AsyncDamageUtil.getAssumedDamage(damageToTarget, target) * autoCrystal.getDamageMultiplier().getValue()) < 0.0f;
    }

    private boolean isArmorBreaker(LivingEntityState target, float damage)
    {
        for (ItemStack armorStack : target.getArmorItems())
        {
            if (armorStack.isEmpty())
            {
                continue;
            }

            int armorDamage = ExplosionUtil.getArmorDurabilityDamage(armorStack, damage);
            float durability = ItemUtil.getDurability(armorStack) - (armorDamage * autoCrystal.getArmorMultiplier().getValue());
            if (durability <= 0)
            {
                return true;
            }
        }

        return false;
    }
}
