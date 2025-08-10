package net.shoreline.client.impl.module.combat.crystal;

import lombok.Data;

@Data
public class CrystalData<T>
{
    private T crystalData;
    private double damageToTarget, damageToPlayer;

    public CrystalData(T crystalData, double damageToTarget, double damageToPlayer)
    {
        this.crystalData = crystalData;
        this.damageToTarget = damageToTarget;
        this.damageToPlayer = damageToPlayer;
    }
}
