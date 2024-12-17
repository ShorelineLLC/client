package net.shoreline.client.util.world;

import net.minecraft.block.BlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.DamageUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.DefaultAttributeRegistry;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;
import net.shoreline.client.util.Globals;
import net.shoreline.client.util.player.EnchantmentUtil;
import org.apache.commons.lang3.mutable.MutableInt;

import java.util.Set;
import java.util.function.BiFunction;

/**
 * @author linus
 * @since 1.0
 */
public class ExplosionUtil implements Globals
{
    /**
     * @param entity
     * @param explosion
     * @return
     */
    public static double getDamageTo(final Entity entity,
                                     final Vec3d explosion,
                                     boolean assumeBestArmor)
    {
        return getDamageTo(entity, explosion, false, assumeBestArmor);
    }

    /**
     * @param entity
     * @param explosion
     * @param ignoreTerrain
     * @return
     */
    public static double getDamageTo(final Entity entity,
                                     final Vec3d explosion,
                                     final boolean ignoreTerrain,
                                     boolean assumeBestArmor)
    {
        return getDamageTo(entity, explosion, ignoreTerrain, 12.0f, 0, assumeBestArmor);
    }

    public static double getDamageTo(final Entity entity,
                                     final Vec3d explosion,
                                     final boolean ignoreTerrain,
                                     int extrapolationTicks,
                                     boolean assumeBestArmor)
    {
        return getDamageTo(entity, explosion, ignoreTerrain, 12.0f, extrapolationTicks, assumeBestArmor);
    }

    public static double getDamageTo(final Entity entity,
                                     final Vec3d explosion,
                                     final IgnoreTerrain ignoreTerrain,
                                     boolean assumeBestArmor)
    {
        return getDamageTo(entity, explosion, ignoreTerrain, 12.0f, 0, assumeBestArmor);
    }

    public static double getDamageTo(final Entity entity,
                                     final Vec3d explosion,
                                     final IgnoreTerrain ignoreTerrain,
                                     int extrapolationTicks,
                                     boolean assumeBestArmor)
    {
        return getDamageTo(entity, explosion, ignoreTerrain, 12.0f, extrapolationTicks, assumeBestArmor);
    }

    public static double getDamageTo(final Entity entity,
                                     final Vec3d explosion,
                                     final IgnoreTerrain ignoreTerrain,
                                     float power,
                                     int extrapolationTicks,
                                     boolean assumeBestArmor)
    {
        double x = entity.getX();
        double y = entity.getY();
        double z = entity.getZ();

        Vec3d vec3d2 = Vec3d.ZERO;
        if (extrapolationTicks != 0)
        {
            double ox = (x - entity.prevX) * extrapolationTicks;
            double oy = (y - entity.prevY) * extrapolationTicks * 0.3;
            double oz = (z - entity.prevZ) * extrapolationTicks;
            x += ox;
            y += oy;
            z += oz;
            vec3d2 = new Vec3d(ox, oy, oz);
        }

        Vec3d vec3d = new Vec3d(x, y, z);
        double d = Math.sqrt(vec3d.squaredDistanceTo(explosion));
        double ab = getExposure(explosion, entity.getBoundingBox().offset(vec3d2), ignoreTerrain);
        double w = d / power;
        double ac = (1.0 - w) * ab;
        double dmg = (float) ((int) ((ac * ac + ac) / 2.0 * 7.0 * 12.0 + 1.0));
        dmg = getReduction(entity, mc.world.getDamageSources().explosion(null), dmg, assumeBestArmor);
        return Math.max(0.0, dmg);
    }

    public static double getDamageTo(final Entity entity,
                                     final Vec3d explosion,
                                     final boolean ignoreTerrain,
                                     final Set<BlockPos> ignoreBlocks,
                                     boolean assumeBestArmor)
    {
        return getDamageTo(entity, explosion, ignoreTerrain, 12.0f, ignoreBlocks, 0, assumeBestArmor);
    }

    public static double getDamageTo(final Entity entity,
                                     final Vec3d explosion,
                                     final boolean ignoreTerrain,
                                     final Set<BlockPos> ignoreBlocks,
                                     int extrapolationTicks,
                                     boolean assumeBestArmor)
    {
        return getDamageTo(entity, explosion, ignoreTerrain, 12.0f, ignoreBlocks, extrapolationTicks, assumeBestArmor);
    }

    public static double getDamageTo(final Entity entity,
                                     final Vec3d explosion,
                                     final boolean ignoreTerrain,
                                     float power,
                                     final Set<BlockPos> ignoreBlocks,
                                     int extrapolationTicks,
                                     boolean assumeBestArmor)
    {
        double x = entity.getX();
        double y = entity.getY();
        double z = entity.getZ();

        Vec3d vec3d2 = Vec3d.ZERO;
        if (extrapolationTicks != 0)
        {
            double ox = (x - entity.prevX) * extrapolationTicks;
            double oy = (y - entity.prevY) * extrapolationTicks * 0.3;
            double oz = (z - entity.prevZ) * extrapolationTicks;
            x += ox;
            y += oy;
            z += oz;
            vec3d2 = new Vec3d(ox, oy, oz);
        }

        Vec3d vec3d = new Vec3d(x, y, z);
        double d = Math.sqrt(vec3d.squaredDistanceTo(explosion));
        double ab = getExposure(explosion, entity.getBoundingBox().offset(vec3d2), ignoreTerrain ? IgnoreTerrain.BLAST : IgnoreTerrain.NONE, ignoreBlocks);
        double w = d / power;
        double ac = (1.0 - w) * ab;
        double dmg = (float) ((int) ((ac * ac + ac) / 2.0 * 7.0 * 12.0 + 1.0));
        dmg = getReduction(entity, mc.world.getDamageSources().explosion(null), dmg, assumeBestArmor);
        return Math.max(0.0, dmg);
    }

    public static double getDamageTo(final Entity entity,
                                     final Vec3d explosion,
                                     final IgnoreTerrain ignoreTerrain,
                                     final Set<BlockPos> ignoreBlocks,
                                     boolean assumeBestArmor)
    {
        return getDamageTo(entity, explosion, ignoreTerrain, 12.0f, ignoreBlocks, 0, assumeBestArmor);
    }

    public static double getDamageTo(final Entity entity,
                                     final Vec3d explosion,
                                     final IgnoreTerrain ignoreTerrain,
                                     float power,
                                     final Set<BlockPos> ignoreBlocks,
                                     int extrapolationTicks,
                                     boolean assumeBestArmor)
    {
        double x = entity.getX();
        double y = entity.getY();
        double z = entity.getZ();

        Vec3d vec3d2 = Vec3d.ZERO;
        if (extrapolationTicks != 0)
        {
            double ox = (x - entity.prevX) * extrapolationTicks;
            double oy = (y - entity.prevY) * extrapolationTicks * 0.3;
            double oz = (z - entity.prevZ) * extrapolationTicks;
            x += ox;
            y += oy;
            z += oz;
            vec3d2 = new Vec3d(ox, oy, oz);
        }

        Vec3d vec3d = new Vec3d(x, y, z);
        double d = Math.sqrt(vec3d.squaredDistanceTo(explosion));
        double ab = getExposure(explosion, entity.getBoundingBox().offset(vec3d2), ignoreTerrain, ignoreBlocks);
        double w = d / power;
        double ac = (1.0 - w) * ab;
        double dmg = (float) ((int) ((ac * ac + ac) / 2.0 * 7.0 * 12.0 + 1.0));
        dmg = getReduction(entity, mc.world.getDamageSources().explosion(null), dmg, assumeBestArmor);
        return Math.max(0.0, dmg);
    }

    /**
     * @param entity
     * @param explosion
     * @return
     */
    public static double getDamageTo(final Entity entity,
                                     final Vec3d explosion,
                                     final boolean ignoreTerrain,
                                     float power,
                                     int extrapolationTicks,
                                     boolean assumeBestArmor)
    {
        double x = entity.getX();
        double y = entity.getY();
        double z = entity.getZ();

        Vec3d vec3d2 = Vec3d.ZERO;
        if (extrapolationTicks != 0)
        {
            double ox = (x - entity.prevX) * extrapolationTicks;
            double oy = (y - entity.prevY) * extrapolationTicks * 0.3;
            double oz = (z - entity.prevZ) * extrapolationTicks;
            x += ox;
            y += oy;
            z += oz;
            vec3d2 = new Vec3d(ox, oy, oz);
        }

        Vec3d vec3d = new Vec3d(x, y, z);
        double d = Math.sqrt(vec3d.squaredDistanceTo(explosion));
        double ab = getExposure(explosion, entity.getBoundingBox().offset(vec3d2), ignoreTerrain ? IgnoreTerrain.BLAST : IgnoreTerrain.NONE);
        double w = d / power;
        double ac = (1.0 - w) * ab;
        double dmg = (float) ((int) ((ac * ac + ac) / 2.0 * 7.0 * 12.0 + 1.0));
        dmg = getReduction(entity, mc.world.getDamageSources().explosion(null), dmg, assumeBestArmor);
        return Math.max(0.0, dmg);
    }

    /**
     * @param pos           The actual position of the damage
     * @param entity
     * @param explosion
     * @param ignoreTerrain
     * @return
     */
    public static double getDamageToPos(final Vec3d pos,
                                        final Entity entity,
                                        final Vec3d explosion,
                                        final boolean ignoreTerrain,
                                        boolean assumeBestArmor)
    {
        final Box bb = entity.getBoundingBox();
        double dx = pos.getX() - bb.minX;
        double dy = pos.getY() - bb.minY;
        double dz = pos.getZ() - bb.minZ;
        final Box box = bb.offset(dx, dy, dz);
        //
        RaycastFactory raycastFactory = getRaycastFactory(ignoreTerrain ? IgnoreTerrain.BLAST : IgnoreTerrain.NONE);
        double ab = getExposure(explosion, box, raycastFactory);
        double w = Math.sqrt(pos.squaredDistanceTo(explosion)) / 12.0;
        double ac = (1.0 - w) * ab;
        double dmg = (float) ((int) ((ac * ac + ac) / 2.0 * 7.0 * 12.0 + 1.0));
        dmg = getReduction(entity, mc.world.getDamageSources().explosion(null), dmg, assumeBestArmor);
        return Math.max(0.0, dmg);
    }

    /**
     * @param entity
     * @param damage
     * @return
     */
    private static double getReduction(Entity entity, DamageSource damageSource, double damage, boolean assumeBestArmor)
    {
        if (damageSource.isScaledWithDifficulty())
        {
            switch (mc.world.getDifficulty())
            {
                // case PEACEFUL -> return 0;
                case EASY -> damage = Math.min(damage / 2 + 1, damage);
                case HARD -> damage *= 1.5f;
            }
        }

        if (entity instanceof LivingEntity livingEntity)
        {
            damage = DamageUtil.getDamageLeft(livingEntity, (float) damage, damageSource, getArmor(livingEntity), (float) livingEntity.getAttributeValue(EntityAttributes.GENERIC_ARMOR_TOUGHNESS));
            damage = getResistanceReduction(livingEntity, damage);
            damage = getProtectionReduction(livingEntity, damage, damageSource, assumeBestArmor);
        }

        return Math.max(damage, 0);
    }

    private static float getArmor(LivingEntity entity)
    {
        return (float) Math.floor(entity.getAttributeValue(EntityAttributes.GENERIC_ARMOR));
    }

    private static float getProtectionReduction(Entity player, double damage, DamageSource source, boolean assumeBestArmor)
    {
        if (player instanceof LivingEntity livingEntity)
        {
            float protLevel = getProtectionAmount(livingEntity.getArmorItems(), assumeBestArmor);
            return DamageUtil.getInflictedDamage((float) damage, protLevel);
        }
        return 0.0f;
    }

    private static float getProtectionAmount(Iterable<ItemStack> equipment, boolean assumeBestArmor)
    {
        MutableInt mutableInt = new MutableInt();
        equipment.forEach(i ->
        {
            if (assumeBestArmor && EnchantmentUtil.isFakeEnchant2b2t(i))
            {
                mutableInt.add(i.getItem() instanceof ArmorItem armorItem && armorItem.getType() == ArmorItem.Type.LEGGINGS ? 8 : 4);
            }
            else
            {
                int modifierBlast = EnchantmentHelper.getLevel(mc.world.getRegistryManager().get(Enchantments.BLAST_PROTECTION.getRegistryRef()).getEntry(Enchantments.BLAST_PROTECTION).get(), i);
                int modifier = EnchantmentHelper.getLevel(mc.world.getRegistryManager().get(Enchantments.PROTECTION.getRegistryRef()).getEntry(Enchantments.PROTECTION).get(), i);
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

    private static <T extends LivingEntity> DefaultAttributeContainer getDefaultForEntity(T entity)
    {
        return DefaultAttributeRegistry.get((EntityType<? extends LivingEntity>) entity.getType());
    }

    /**
     * @param source
     * @param box
     * @param ignoreTerrain
     * @return
     */
    private static float getExposure(final Vec3d source,
                                     final Box box,
                                     final IgnoreTerrain ignoreTerrain,
                                     final Set<BlockPos> ignoreBlocks)
    {
        RaycastFactory raycastFactory = getRaycastFactory(ignoreTerrain, ignoreBlocks);
        return getExposure(source, box, raycastFactory);
    }

    /**
     * @param source
     * @param box
     * @param ignoreTerrain
     * @return
     */
    private static float getExposure(final Vec3d source,
                                     final Box box,
                                     final IgnoreTerrain ignoreTerrain)
    {
        RaycastFactory raycastFactory = getRaycastFactory(ignoreTerrain);
        return getExposure(source, box, raycastFactory);
    }

    /**
     * @param source
     * @param box
     * @return
     */
    private static float getExposure(final Vec3d source,
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

    private static RaycastFactory getRaycastFactory(IgnoreTerrain ignoreTerrain, Set<BlockPos> ignoreBlocks)
    {
        if (ignoreTerrain == IgnoreTerrain.BLAST)
        {
            return (context, blockPos) ->
            {
                if (ignoreBlocks.contains(blockPos))
                {
                    return null;
                }
                BlockState blockState = mc.world.getBlockState(blockPos);
                if (blockState.getBlock().getBlastResistance() < 600) return null;

                return blockState.getCollisionShape(mc.world, blockPos).raycast(context.start(), context.end(), blockPos);
            };
        }
        else if (ignoreTerrain == IgnoreTerrain.ALL)
        {
            return (context, blockPos) -> null;
        }
        else
        {
            return (context, blockPos) ->
            {
                if (ignoreBlocks.contains(blockPos))
                {
                    return null;
                }
                BlockState blockState = mc.world.getBlockState(blockPos);
                return blockState.getCollisionShape(mc.world, blockPos).raycast(context.start(), context.end(), blockPos);
            };
        }
    }

    private static RaycastFactory getRaycastFactory(IgnoreTerrain ignoreTerrain)
    {
        if (ignoreTerrain == IgnoreTerrain.BLAST)
        {
            return (context, blockPos) ->
            {
                BlockState blockState = mc.world.getBlockState(blockPos);
                if (blockState.getBlock().getBlastResistance() < 600) return null;

                return blockState.getCollisionShape(mc.world, blockPos).raycast(context.start(), context.end(), blockPos);
            };
        }
        else if (ignoreTerrain == IgnoreTerrain.ALL)
        {
            return (context, blockPos) -> null;
        }
        else
        {
            return (context, blockPos) ->
            {
                BlockState blockState = mc.world.getBlockState(blockPos);
                return blockState.getCollisionShape(mc.world, blockPos).raycast(context.start(), context.end(), blockPos);
            };
        }
    }

    /* Raycasts */

    private static BlockHitResult raycast(ExposureRaycastContext context, RaycastFactory raycastFactory)
    {
        return BlockView.raycast(context.start, context.end, context, raycastFactory, ctx -> null);
    }

    public record ExposureRaycastContext(Vec3d start, Vec3d end)
    {
    }

    @FunctionalInterface
    public interface RaycastFactory extends BiFunction<ExposureRaycastContext, BlockPos, BlockHitResult>
    {
    }

    public enum IgnoreTerrain
    {
        ALL,
        BLAST,
        NONE
    }
}
