package net.shoreline.client.impl.module.combat.crystal;

import lombok.Data;

@Data
public class CrystalData<T>
{
    private T crystalData;
    private double damageToTarget, damageToPlayer;
}
