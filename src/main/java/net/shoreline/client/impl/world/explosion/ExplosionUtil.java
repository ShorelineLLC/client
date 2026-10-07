package net.shoreline.client.impl.world.explosion;

import lombok.experimental.UtilityClass;
import net.minecraft.client.MinecraftClient;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.DamageUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;
import net.shoreline.client.impl.module.client.InventoryModule;
import net.shoreline.client.util.item.EnchantUtil;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

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
        float dmg = ExplosionTrace.getDamageToPos(blockView,
                explosion,
                entity.getPos(),
                entity.getBoundingBox(),
                power,
                ignoreTerrain,
                ignoreBlocks);

        return getAppliedDamageToEntity(entity, dmg);
    }

    public float getAppliedDamageToEntity(Entity entity, float damage)
    {
        return Math.max(0.0f, getReduction(entity, MinecraftClient.getInstance().world.getDamageSources().explosion(null), damage));
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
            damage = DamageUtil.getDamageLeft(livingEntity, damage, damageSource, getArmor(livingEntity), (float) getAttributeValue(livingEntity, EntityAttributes.ARMOR_TOUGHNESS));
            damage = getResistanceReduction(livingEntity, damage);
            damage = getProtectionReduction(livingEntity, damage);
        }

        return damage;
    }

    private float getArmor(LivingEntity entity)
    {
        return (float) Math.floor(getAttributeValue(entity, EntityAttributes.ARMOR));
    }

    // TODO: Figure out why this is null
    private double getAttributeValue(LivingEntity entity, RegistryEntry<EntityAttribute> attribute)
    {
        try
        {
            return entity.getAttributeValue(attribute);
        } catch (NullPointerException ignored)
        {
            return 0.0;
        }
    }

    private float getProtectionReduction(Entity player, float damage)
    {
        if (player instanceof LivingEntity livingEntity)
        {
            float protLevel = getProtectionAmount(livingEntity.getArmorItems());
            return DamageUtil.getInflictedDamage(damage, protLevel);
        }

        return 0.0f;
    }

    private float getProtectionAmount(Iterable<ItemStack> equipment)
    {
        MutableInt mutableInt = new MutableInt();
        equipment.forEach(i ->
        {
            if (InventoryModule.INSTANCE.getAssumeEnchanted().getValue() && EnchantUtil.isEnchantsObfuscated(i))
            {
                ComponentMap item = i.getItem().getComponents();
                if (item.contains(DataComponentTypes.EQUIPPABLE))
                {
                    mutableInt.add(item.get(DataComponentTypes.EQUIPPABLE).slot().getIndex() == 2 ? 8 : 4);
                }
            } else
            {
                ItemEnchantmentsComponent enchantments = EnchantmentHelper.getEnchantments(i);
                for (RegistryEntry<Enchantment> enchantment : enchantments.getEnchantments())
                {
                    if (enchantment.getIdAsString().contains("protection"))
                    {
                        mutableInt.add(enchantments.getLevel(enchantment));
                    }

                    if (enchantment.getIdAsString().contains("blast_protection"))
                    {
                        mutableInt.add(enchantments.getLevel(enchantment) * 2);
                    }
                }
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

    public int getArmorDurabilityDamage(ItemStack stack, float damage)
    {
        int dmg = (int) (damage / 4.0f);
        if (dmg < 1)
        {
            dmg = 1;
        }

        int level = EnchantUtil.getLevel(Enchantments.UNBREAKING, stack);
        double p = 0.6 + 0.4 / (level + 1.0);
        int result = 0;
        for (int i = 0; i < dmg; i++)
        {
            if (ThreadLocalRandom.current().nextDouble() < p)
            {
                result++;
            }
        }

        return result;
    }
}