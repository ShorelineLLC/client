package net.shoreline.client.util.world;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.AmbientEntity;
import net.minecraft.entity.mob.Angerable;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.passive.SquidEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.entity.vehicle.ChestMinecartEntity;
import net.minecraft.entity.vehicle.FurnaceMinecartEntity;
import net.minecraft.entity.vehicle.MinecartEntity;

/**
 * @author linus
 * @since 1.0
 */
public class EntityUtil {

    /**
     * @param entity
     * @return
     */
    public static float getHealth(Entity entity) {
        if (entity instanceof LivingEntity e) {
            return e.getHealth() + e.getAbsorptionAmount();
        }
        return 0.0f;
    }

    /**
     * @param e
     * @return
     */
    public static boolean isMonster(Entity e) {
        return e instanceof Monster && !isNeutral(e);
    }

    /**
     * @param e
     * @return
     */
    public static boolean isNeutral(Entity e) {
        return e instanceof Angerable && !((Angerable) e).hasAngerTime();
    }

    /**
     * @param e
     * @return
     */
    public static boolean isPassive(Entity e) {
        return e instanceof PassiveEntity || e instanceof AmbientEntity
                || e instanceof SquidEntity;
    }

    public static boolean isVehicle(Entity e) {
        return e instanceof BoatEntity || e instanceof MinecartEntity
                || e instanceof FurnaceMinecartEntity
                || e instanceof ChestMinecartEntity;
    }
}
