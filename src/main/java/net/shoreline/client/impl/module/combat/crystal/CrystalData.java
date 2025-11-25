package net.shoreline.client.impl.module.combat.crystal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import net.minecraft.util.math.Vec3d;
import net.shoreline.client.impl.world.EntityState;
import net.shoreline.client.impl.world.LivingEntityState;

@AllArgsConstructor
@Data
public class CrystalData<T>
{
    private T value;

    private Vec3d crystalVec;

    @EqualsAndHashCode.Exclude
    private LivingEntityState target;

    @EqualsAndHashCode.Exclude
    private double damageToTarget, damageToPlayer;

    public static class Immediate<T> extends CrystalData<T>
    {
        public Immediate(T value,
                         Vec3d crystalVec,
                         LivingEntityState target)
        {
            super(value, crystalVec, target, 0.0f, 0.0f);
        }
    }

    @Getter
    public static class WithRotation<T> extends Immediate<T>
    {
        private final float[] angles;

        public WithRotation(T value,
                            Vec3d crystalVec,
                            LivingEntityState target,
                            float[] angles)
        {
            super(value, crystalVec, target);
            this.angles = angles;
        }
    }
}
