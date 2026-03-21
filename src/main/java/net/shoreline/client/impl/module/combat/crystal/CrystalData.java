package net.shoreline.client.impl.module.combat.crystal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode.Exclude;
import lombok.Getter;
import net.minecraft.util.math.Vec3d;
import net.shoreline.client.impl.render.animation.Animation;
import net.shoreline.client.impl.world.LivingEntityState;

@AllArgsConstructor
@Data
public class CrystalData<T>
{
    private T value;
    private Vec3d crystalVec;

    @Exclude
    private LivingEntityState target;

    @Exclude
    private double damageToTarget, damageToPlayer;

    @Exclude
    private final Animation animation = new Animation(true, 250);

    public void copyFrom(CrystalData<T> crystalData)
    {
        this.crystalVec = crystalData.getCrystalVec();
        this.target = crystalData.getTarget();
        this.damageToTarget = crystalData.getDamageToTarget();
        this.damageToPlayer = crystalData.getDamageToPlayer();
    }

    @Getter
    public static class Immediate<T> extends CrystalData<T>
    {
        private final String tag;

        public Immediate(String tag,
                         T value,
                         Vec3d crystalVec,
                         LivingEntityState target,
                         float damageToTarget,
                         float damageToPlayer)
        {
            super(value, crystalVec, target, damageToTarget, damageToPlayer);
            this.tag = tag;
        }

        public Immediate(T value,
                         Vec3d crystalVec,
                         LivingEntityState target,
                         float damageToTarget,
                         float damageToPlayer)
        {
            this(null, value, crystalVec, target, damageToTarget, damageToPlayer);
        }
    }
}
