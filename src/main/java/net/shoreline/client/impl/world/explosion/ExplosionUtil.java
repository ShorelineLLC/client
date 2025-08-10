package net.shoreline.client.impl.world.explosion;

import lombok.experimental.UtilityClass;
import net.minecraft.client.MinecraftClient;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.DamageUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;
import net.shoreline.client.impl.module.client.AnticheatModule;
import net.shoreline.client.impl.world.EntityState;
import net.shoreline.client.util.item.EnchantUtil;
import org.apache.commons.lang3.mutable.MutableInt;

import java.util.Collections;
import java.util.Set;

@UtilityClass
public class ExplosionUtil
{
    public double crystalDamageToEntity(final BlockView blockView,
                                        final LivingEntity entity,
                                        final Vec3d explosion)
    {
        return crystalDamageToEntity(blockView, entity, explosion, false, Collections.emptySet());
    }

    public double crystalDamageToEntity(final BlockView blockView,
                                        final LivingEntity entity,
                                        final Vec3d explosion,
                                        final boolean ignoreTerrain,
                                        final Set<BlockPos> ignoreBlocks)
    {
        return damageToEntity(blockView, entity, explosion, 12.0f, ignoreTerrain, ignoreBlocks);
    }

    public double damageToEntity(final BlockView blockView,
                                 final LivingEntity entity,
                                 final Vec3d explosion,
                                 final float power,
                                 final boolean ignoreTerrain,
                                 final Set<BlockPos> ignoreBlocks)
    {
        float dmg = ExplosionTrace.getDamageToPos(blockView, explosion, entity.getPos(), entity.getBoundingBox(), power, ignoreTerrain, ignoreBlocks);
        return getAppliedDamageToEntity(entity, dmg);
    }

    public float getAppliedDamageToEntity(LivingEntity livingEntity, float damage)
    {
        return Math.max(0.0f, getReduction(livingEntity, MinecraftClient.getInstance().world.getDamageSources().explosion(null), damage));
    }

    private float getReduction(Entity entity, DamageSource damageSource, float damage)
    {
        if (damageSource.isScaledWithDifficulty())
        {
            switch (MinecraftClient.getInstance().world.getDifficulty())
            {
                // case PEACEFUL -> return 0;
                case EASY -> damage = Math.min(damage / 2 + 1, damage);
                case HARD -> damage *= 1.5f;
            }
        }

        if (entity instanceof LivingEntity livingEntity)
        {
            damage = DamageUtil.getDamageLeft(livingEntity, damage, damageSource, getArmor(livingEntity), (float) livingEntity.getAttributeValue(EntityAttributes.ARMOR_TOUGHNESS));
            damage = getResistanceReduction(livingEntity, damage);
            damage = getProtectionReduction(livingEntity, damage);
        }

        return damage;
    }

    private float getArmor(LivingEntity entity)
    {
        return (float) Math.floor(entity.getAttributeValue(EntityAttributes.ARMOR));
    }

    private float getProtectionReduction(Entity player, float damage)
    {
        if (player instanceof LivingEntity livingEntity)
        {
            float protLevel = getProtectionAmount(livingEntity);
            return DamageUtil.getInflictedDamage(damage, protLevel);
        }

        return 0.0f;
    }

    private float getProtectionAmount(LivingEntity livingEntity)
    {
        MutableInt mutableInt = new MutableInt();
        livingEntity.getArmorItems().forEach(stack ->
        {
            if (AnticheatModule.INSTANCE.isAssumeEnchanted() && EnchantUtil.isEnchantsObfuscated(stack))
            {
                mutableInt.add(livingEntity.getPreferredEquipmentSlot(stack) == EquipmentSlot.LEGS ? 8 : 4);
            }
            else
            {
                int modifierBlast = EnchantUtil.getLevel(Enchantments.BLAST_PROTECTION, stack);
                int modifier = EnchantUtil.getLevel(Enchantments.PROTECTION, stack);
                mutableInt.add(modifierBlast * 2 + modifier);
            }

        });

        return mutableInt.intValue();
    }

    private float getResistanceReduction(LivingEntity player, float damage)
    {
        StatusEffectInstance resistance = player.getStatusEffect(StatusEffects.RESISTANCE);
        if (resistance != null)
        {
            int lvl = resistance.getAmplifier() + 1;
            damage *= (1.0f - (lvl * 0.2f));
        }

        return Math.max(damage, 0.0f);
    }
}