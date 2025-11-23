package net.shoreline.client.impl.module.combat.crystal;

import lombok.*;
import net.minecraft.util.math.Vec3d;
import net.shoreline.client.impl.world.EntityState;

@AllArgsConstructor
@Data
public class CrystalData<T>
{
    private T value;

    private Vec3d crystalVec;

    @EqualsAndHashCode.Exclude
    private EntityState target;

    @EqualsAndHashCode.Exclude
    private double damageToTarget, damageToPlayer;

    public static class Immediate<T> extends CrystalData<T>
    {
        public Immediate(T value,
                         Vec3d crystalVec,
                         EntityState target)
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
                            EntityState target,
                            float[] angles)
        {
            super(value, crystalVec, target);
            this.angles = angles;
        }
    }
}
