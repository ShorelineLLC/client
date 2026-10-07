package net.shoreline.client.impl.module.combat.util;

import lombok.experimental.UtilityClass;
import net.shoreline.client.impl.world.LivingEntityState;

@UtilityClass
public class AsyncDamageUtil
{
    private static final float ASSUMED_ARMOR_REDUCTION = 0.11f;

    public float getAssumedDamage(float baseDamage, LivingEntityState state)
    {
        return state.getTotalArmor() > 0 ? baseDamage * ASSUMED_ARMOR_REDUCTION : baseDamage;
    }
}
