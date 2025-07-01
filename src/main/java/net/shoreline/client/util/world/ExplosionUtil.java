package net.shoreline.client.util.world;

import lombok.experimental.UtilityClass;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.DamageUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;
import net.shoreline.client.impl.module.client.AnticheatModule;
import net.shoreline.client.util.item.EnchantUtil;
import org.apache.commons.lang3.mutable.MutableInt;

import java.util.Collections;
import java.util.Set;
import java.util.function.BiFunction;

@UtilityClass
public class ExplosionUtil
{
    public double damageToEntity(final Entity entity,
                                 final Vec3d explosion)
    {
        return damageToEntity(entity, Vec3d.ZERO, explosion, 12.0f, IgnoreTerrain.NONE, Collections.emptySet());
    }

    public double damageToEntity(final Entity entity,
                                 final Vec3d explosion,
                                 final IgnoreTerrain ignoreTerrain,
                                 final Set<BlockPos> ignoreBlocks)
    {
        return damageToEntity(entity, Vec3d.ZERO, explosion, 12.0f, ignoreTerrain, ignoreBlocks);
    }

    public double damageToEntity(final Entity entity,
                                 final Vec3d offsetPos,
                                 final Vec3d explosion,
                                 final float power,
                                 final IgnoreTerrain ignoreTerrain,
                                 final Set<BlockPos> ignoreBlocks)
    {
        double d = Math.sqrt(entity.getPos().squaredDistanceTo(explosion));
        RaycastFactory raycastFactory = getRaycastFactory(ignoreTerrain, ignoreBlocks);
        double ab = getExposure(explosion, entity.getBoundingBox().offset(offsetPos), raycastFactory);
        double w = d / power;
        double ac = (1.0 - w) * ab;
        double dmg = (float) ((int) ((ac * ac + ac) / 2.0 * 7.0 * 12.0 + 1.0));
        dmg = getReduction(entity, MinecraftClient.getInstance().world.getDamageSources().explosion(null), dmg);
        return Math.max(0.0, dmg);
    }

    private double getReduction(Entity entity, DamageSource damageSource, double damage)
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
            damage = DamageUtil.getDamageLeft(livingEntity, (float) damage, damageSource, getArmor(livingEntity), (float) livingEntity.getAttributeValue(EntityAttributes.ARMOR_TOUGHNESS));
            damage = getResistanceReduction(livingEntity, damage);
            damage = getProtectionReduction(livingEntity, damage);
        }

        return Math.max(damage, 0);
    }

    private float getArmor(LivingEntity entity)
    {
        return (float) Math.floor(entity.getAttributeValue(EntityAttributes.ARMOR));
    }

    private float getProtectionReduction(Entity player, double damage)
    {
        if (player instanceof LivingEntity livingEntity)
        {
            float protLevel = getProtectionAmount(livingEntity, AttributeModifierSlot.ARMOR);
            return DamageUtil.getInflictedDamage((float) damage, protLevel);
        }
        return 0.0f;
    }

    private float getProtectionAmount(LivingEntity livingEntity, AttributeModifierSlot slots)
    {
        MutableInt mutableInt = new MutableInt();
        slots.forEach(i ->
        {
            ItemStack stack = livingEntity.getEquippedStack(i);
            if (AnticheatModule.INSTANCE.isAssumeEnchanted() && EnchantUtil.isEnchantsObfuscated(stack))
            {
                mutableInt.add(i.getEntitySlotId() == 1 ? 8 : 4);
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

    private static double getResistanceReduction(LivingEntity player, double damage)
    {
        StatusEffectInstance resistance = player.getStatusEffect(StatusEffects.RESISTANCE);
        if (resistance != null)
        {
            int lvl = resistance.getAmplifier() + 1;
            damage *= (1.0f - (lvl * 0.2f));
        }

        return Math.max(damage, 0.0f);
    }

    private float getExposure(final Vec3d source,
                              final Box box,
                              final RaycastFactory raycastFactory)
    {
        double xDiff = box.maxX - box.minX;
        double yDiff = box.maxY - box.minY;
        double zDiff = box.maxZ - box.minZ;

        double xStep = 1 / (xDiff * 2 + 1);
        double yStep = 1 / (yDiff * 2 + 1);
        double zStep = 1 / (zDiff * 2 + 1);

        if (xStep > 0 && yStep > 0 && zStep > 0)
        {
            int misses = 0;
            int hits = 0;

            double xOffset = (1 - Math.floor(1 / xStep) * xStep) * 0.5;
            double zOffset = (1 - Math.floor(1 / zStep) * zStep) * 0.5;

            xStep = xStep * xDiff;
            yStep = yStep * yDiff;
            zStep = zStep * zDiff;

            double startX = box.minX + xOffset;
            double startY = box.minY;
            double startZ = box.minZ + zOffset;
            double endX = box.maxX + xOffset;
            double endY = box.maxY;
            double endZ = box.maxZ + zOffset;

            for (double x = startX; x <= endX; x += xStep)
            {
                for (double y = startY; y <= endY; y += yStep)
                {
                    for (double z = startZ; z <= endZ; z += zStep)
                    {
                        Vec3d position = new Vec3d(x, y, z);

                        if (raycast(new ExposureRaycastContext(position, source), raycastFactory) == null) misses++;

                        hits++;
                    }
                }
            }

            return (float) misses / hits;
        }

        return 0f;
    }

    private RaycastFactory getRaycastFactory(IgnoreTerrain ignoreTerrain,
                                             Set<BlockPos> ignoreBlocks)
    {
        if (ignoreTerrain == IgnoreTerrain.BLAST)
        {
            return (context, blockPos) ->
            {
                if (ignoreBlocks.contains(blockPos))
                {
                    return null;
                }
                BlockState blockState = MinecraftClient.getInstance().world.getBlockState(blockPos);
                if (blockState.getBlock().getBlastResistance() < 600)
                {
                    return null;
                }

                return blockState.getCollisionShape(MinecraftClient.getInstance().world, blockPos).raycast(context.start(), context.end(), blockPos);
            };
        } else if (ignoreTerrain == IgnoreTerrain.ALL)
        {
            return (context, blockPos) -> null;
        } else
        {
            return (context, blockPos) ->
            {
                if (ignoreBlocks.contains(blockPos))
                {
                    return null;
                }
                BlockState blockState = MinecraftClient.getInstance().world.getBlockState(blockPos);
                return blockState.getCollisionShape(MinecraftClient.getInstance().world, blockPos).raycast(context.start(), context.end(), blockPos);
            };
        }
    }

    private BlockHitResult raycast(ExposureRaycastContext context,
                                   RaycastFactory raycastFactory)
    {
        return BlockView.raycast(context.start, context.end, context, raycastFactory, ctx -> null);
    }

    public record ExposureRaycastContext(Vec3d start, Vec3d end) {}

    @FunctionalInterface
    public interface RaycastFactory extends BiFunction<ExposureRaycastContext, BlockPos, BlockHitResult> {}

    public enum IgnoreTerrain
    {
        ALL,
        BLAST,
        NONE
    }
}