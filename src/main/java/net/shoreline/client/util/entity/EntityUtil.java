package net.shoreline.client.util.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.*;
import net.minecraft.entity.passive.*;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.entity.vehicle.ChestMinecartEntity;
import net.minecraft.entity.vehicle.FurnaceMinecartEntity;
import net.minecraft.entity.vehicle.MinecartEntity;
import net.minecraft.util.math.BlockPos;
import net.shoreline.client.util.Globals;
import net.shoreline.client.util.chat.ChatUtil;

/**
 * @author linus
 * @since 1.0
 */
public class EntityUtil implements Globals
{
    /**
     *
     * @param entity
     * @return
     */
    public static BlockPos getRoundedBlockPos(Entity entity)
    {
        return new BlockPos(entity.getBlockX(), (int) Math.round(entity.getY()), entity.getBlockZ());
    }

    /**
     * @param entity
     * @return
     */
    public static float getHealth(Entity entity)
    {
        if (entity instanceof LivingEntity e)
        {
            return e.getHealth() + e.getAbsorptionAmount();
        }
        return 0.0f;
    }

    /**
     * @param e
     * @return
     */
    public static boolean isMonster(Entity e)
    {
        return e instanceof Monster && !isNeutralInternal(e);
    }

    private static boolean isNeutralInternal(Entity e)
    {
        return e instanceof EndermanEntity enderman && !enderman.isAttacking()
                || e instanceof ZombifiedPiglinEntity piglin && !piglin.isAttacking()
                || e instanceof WolfEntity wolf && !wolf.isAttacking()
                || e instanceof IronGolemEntity ironGolem && !ironGolem.isAttacking()
                || e instanceof BeeEntity bee && !bee.isAttacking();
    }

    /**
     * @param e
     * @return
     */
    public static boolean isNeutral(Entity e)
    {
        return e instanceof EndermanEntity || e instanceof ZombifiedPiglinEntity || e instanceof WolfEntity || e instanceof IronGolemEntity;
    }

    /**
     * @param e
     * @return
     */
    public static boolean isPassive(Entity e)
    {
        return e instanceof PassiveEntity || e instanceof AmbientEntity || e instanceof SquidEntity;
    }

    public static boolean isVehicle(Entity e)
    {
        return e instanceof BoatEntity || e instanceof MinecartEntity
                || e instanceof FurnaceMinecartEntity
                || e instanceof ChestMinecartEntity;
    }
}
