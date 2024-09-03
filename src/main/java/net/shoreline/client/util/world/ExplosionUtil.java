package net.shoreline.client.util.world;

import com.google.common.collect.Multimap;
import net.minecraft.block.BlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.*;
import net.minecraft.entity.attribute.*;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;
import net.shoreline.client.util.Globals;

import java.util.List;
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
                                     final Vec3d explosion)
    {
        return getDamageTo(entity, explosion, false);
    }

    /**
     * @param entity
     * @param explosion
     * @param ignoreTerrain
     * @return
     */
    public static double getDamageTo(final Entity entity,
                                     final Vec3d explosion,
                                     final boolean ignoreTerrain)
    {
        return getDamageTo(entity, explosion, ignoreTerrain, 12.0f);
    }

    public static double getDamageTo(final Entity entity,
                                     final Vec3d explosion,
                                     final IgnoreTerrain ignoreTerrain)
    {
        return getDamageTo(entity, explosion, ignoreTerrain, 12.0f);
    }

    public static double getDamageTo(final Entity entity,
                                     final Vec3d explosion,
                                     final IgnoreTerrain ignoreTerrain,
                                     float power)
    {
        double d = Math.sqrt(entity.squaredDistanceTo(explosion));
        double ab = getExposure(explosion, entity, ignoreTerrain);
        double w = d / power;
        double ac = (1.0 - w) * ab;
        double dmg = (float) ((int) ((ac * ac + ac) / 2.0 * 7.0 * 12.0 + 1.0));
        dmg = getReduction(entity, mc.world.getDamageSources().explosion(null), dmg);
        return Math.max(0.0, dmg);
    }

    public static double getDamageTo(final Entity entity,
                                     final Vec3d explosion,
                                     final IgnoreTerrain ignoreTerrain,
                                     final BlockPos... ignoreBlocks)
    {
        return getDamageTo(entity, explosion, ignoreTerrain, 12.0f, ignoreBlocks);
    }

    public static double getDamageTo(final Entity entity,
                                     final Vec3d explosion,
                                     final IgnoreTerrain ignoreTerrain,
                                     float power,
                                     final BlockPos... ignoreBlocks)
    {
        double d = Math.sqrt(entity.squaredDistanceTo(explosion));
        double ab = getExposure(explosion, entity, ignoreTerrain, ignoreBlocks);
        double w = d / power;
        double ac = (1.0 - w) * ab;
        double dmg = (float) ((int) ((ac * ac + ac) / 2.0 * 7.0 * 12.0 + 1.0));
        dmg = getReduction(entity, mc.world.getDamageSources().explosion(null), dmg);
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
                                     float power)
    {
        double d = Math.sqrt(entity.squaredDistanceTo(explosion));
        double ab = getExposure(explosion, entity, ignoreTerrain ? IgnoreTerrain.BLAST : IgnoreTerrain.NONE);
        double w = d / power;
        double ac = (1.0 - w) * ab;
        double dmg = (float) ((int) ((ac * ac + ac) / 2.0 * 7.0 * 12.0 + 1.0));
        dmg = getReduction(entity, mc.world.getDamageSources().explosion(null), dmg);
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
                                        final boolean ignoreTerrain)
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
        dmg = getReduction(entity, mc.world.getDamageSources().explosion(null), dmg);
        return Math.max(0.0, dmg);
    }

    /**
     * @param entity
     * @param damage
     * @return
     */
    private static double getReduction(Entity entity, DamageSource damageSource, double damage)
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
            damage = DamageUtil.getDamageLeft((float) damage, getArmor(livingEntity), (float) getAttributeValue(livingEntity, EntityAttributes.GENERIC_ARMOR_TOUGHNESS));
            damage = getProtectionReduction(entity, damage, damageSource);
        }
        return Math.max(damage, 0);
    }

    private static float getArmor(LivingEntity entity)
    {
        return (float) Math.floor(getAttributeValue(entity, EntityAttributes.GENERIC_ARMOR));
    }

    private static float getProtectionReduction(Entity player, double damage, DamageSource source)
    {
        int protLevel = EnchantmentHelper.getProtectionAmount(player.getArmorItems(), source);
        return DamageUtil.getInflictedDamage((float) damage, protLevel);
    }

    public static double getAttributeValue(LivingEntity entity, EntityAttribute attribute)
    {
        return getAttributeInstance(entity, attribute).getValue();
    }

    public static EntityAttributeInstance getAttributeInstance(LivingEntity entity, EntityAttribute attribute)
    {
        double baseValue = getDefaultForEntity(entity).getBaseValue(attribute);
        EntityAttributeInstance attributeInstance = new EntityAttributeInstance(attribute, o1 ->
        {
        });
        attributeInstance.setBaseValue(baseValue);
        for (var equipmentSlot : EquipmentSlot.values())
        {
            ItemStack stack = entity.getEquippedStack(equipmentSlot);
            Multimap<EntityAttribute, EntityAttributeModifier> modifiers = stack.getAttributeModifiers(equipmentSlot);
            for (var modifier : modifiers.get(attribute)) attributeInstance.addTemporaryModifier(modifier);
        }
        return attributeInstance;
    }

    private static <T extends LivingEntity> DefaultAttributeContainer getDefaultForEntity(T entity)
    {
        return DefaultAttributeRegistry.get((EntityType<? extends LivingEntity>) entity.getType());
    }

    /**
     * @param source
     * @param entity
     * @param ignoreTerrain
     * @return
     */
    private static float getExposure(final Vec3d source,
                                     final Entity entity,
                                     final IgnoreTerrain ignoreTerrain,
                                     final BlockPos... ignoreBlocks)
    {
        RaycastFactory raycastFactory = getRaycastFactory(ignoreTerrain, List.of(ignoreBlocks));
        final Box box = entity.getBoundingBox();
        return getExposure(source, box, raycastFactory);
    }

    /**
     * @param source
     * @param entity
     * @param ignoreTerrain
     * @return
     */
    private static float getExposure(final Vec3d source,
                                     final Entity entity,
                                     final IgnoreTerrain ignoreTerrain)
    {
        RaycastFactory raycastFactory = getRaycastFactory(ignoreTerrain);
        final Box box = entity.getBoundingBox();
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

    private static RaycastFactory getRaycastFactory(IgnoreTerrain ignoreTerrain, List<BlockPos> ignoreBlocks)
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
