package net.shoreline.client.impl.module.combat.crystal;

import lombok.Data;
import net.shoreline.client.impl.world.EntityState;

@Data
public class CrystalData<T>
{
    private EntityState target;
    private T crystalData;
    private double damageToTarget, damageToPlayer;

    public CrystalData(T crystalData, EntityState target, double damageToTarget, double damageToPlayer)
    {
        this.crystalData = crystalData;
        this.target = target;
        this.damageToTarget = damageToTarget;
        this.damageToPlayer = damageToPlayer;
    }
}
