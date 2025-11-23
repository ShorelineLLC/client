package net.shoreline.client.impl.module.combat.crystal;

import lombok.RequiredArgsConstructor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.shoreline.client.impl.world.EntityState;

@RequiredArgsConstructor
public class CrystalDataFactory
{
    private final CrystalEntityScanner view;

    public <T> CrystalData<T> createData(T value,
                                         Vec3d crystalVec,
                                         EntityState target,
                                         float damageToTarget,
                                         float damageToPlayer)
    {
        BlockPos blockPos = BlockPos.ofFloored(crystalVec);

        if (view.isAntiSurroundPos(blockPos))
        {
            return new CrystalData.Immediate<>(value, crystalVec, target);
        }

        return new CrystalData<>(value, crystalVec, target, damageToTarget, damageToPlayer);
    }
}
