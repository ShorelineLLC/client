package net.shoreline.client.impl.module.combat.crystal;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.shoreline.client.impl.module.combat.AutoCrystalModule;
import net.shoreline.client.impl.module.combat.util.DamageUtil;
import net.shoreline.client.impl.world.AsyncWorldScanner;
import net.shoreline.client.impl.world.EntityState;
import net.shoreline.client.util.world.ExplosionUtil;

public class CrystalBaseScanner extends AsyncWorldScanner
{
    private final AutoCrystalModule autoCrystal = AutoCrystalModule.INSTANCE;

    @Override
    protected void visit(BlockPos pos, BlockState state)
    {
        if (!CrystalUtil.canUseOnBlock(pos, state))
        {
            return;
        }

//        Vec3d explosionCenter = Vec3d.of(pos).add(0.5, 1.0, 0.5);
//        double local = ExplosionUtil.damageToEntity(localEntity, explosionCenter);
//        if (local > autoCrystal.getMaxSelfDamage().getValue() || DamageUtil.willDamageKillEntity(local, localEntity))
//        {
//            return;
//        }
//
//        for (EntityState entity : getEntities())
//        {
//            double blockDist = pos.getSquaredDistance(entity.getPos());
//            if (blockDist > 144.0f)
//            {
//                continue;
//            }
//
//
//        }
    }

    @Override
    protected int getRadius()
    {
        return (int) Math.ceil(autoCrystal.getPlaceRange().getValue());
    }
}
