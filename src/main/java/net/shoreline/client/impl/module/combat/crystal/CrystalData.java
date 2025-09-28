package net.shoreline.client.impl.module.combat.crystal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
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

    private boolean antiSurround;
}
