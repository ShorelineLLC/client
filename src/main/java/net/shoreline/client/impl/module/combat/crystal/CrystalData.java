package net.shoreline.client.impl.module.combat.crystal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.shoreline.client.impl.world.EntityState;

@AllArgsConstructor
@Data
public class CrystalData<T>
{
    private T crystalData;

    @EqualsAndHashCode.Exclude
    private EntityState target;

    @EqualsAndHashCode.Exclude
    private double damageToTarget, damageToPlayer;

    private boolean antiSurround;
}
