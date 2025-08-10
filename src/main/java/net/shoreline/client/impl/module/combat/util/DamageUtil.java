package net.shoreline.client.impl.module.combat.util;

import lombok.experimental.UtilityClass;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.math.MathHelper;
import net.shoreline.client.impl.world.explosion.ExplosionUtil;

@UtilityClass
public class DamageUtil
{
    public float getHealth(LivingEntity entity)
    {
        return entity.getHealth() + entity.getAbsorptionAmount();
    }

    public boolean willDamageKillEntity(double damage, LivingEntity entity)
    {
        return getHealth(entity) - damage < 0.5f;
    }

    public double getCrystalDamage(LivingEntity entity)
    {
        double crystalDmg = 0.0;
        for (Entity e : MinecraftClient.getInstance().world.getEntities())
        {
            if (e instanceof EndCrystalEntity crystal && entity.squaredDistanceTo(e) <= 144.0)
            {
                double damage = ExplosionUtil.crystalDamageToEntity(MinecraftClient.getInstance().world, entity, crystal.getPos());
                if (damage > crystalDmg)
                {
                    crystalDmg = damage;
                }
            }
        }
        return crystalDmg;
    }

    public int getFallDamage(LivingEntity entity, double fallDistance, float damageMultiplier)
    {
        final StatusEffectInstance statusEffectInstance = entity.getStatusEffect(StatusEffects.JUMP_BOOST);
        final float f = statusEffectInstance == null ? 0.0f : (float) (statusEffectInstance.getAmplifier() + 1);
        return MathHelper.ceil((fallDistance - 3.0f - f) * damageMultiplier);
    }
}
