package net.shoreline.client.impl.module.combat.helper;

import net.minecraft.block.BlockState;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.shoreline.client.impl.module.combat.AutoCrystalModule;
import net.shoreline.client.impl.module.helper.BlockScanner;
import net.shoreline.client.util.world.ExplosionUtil;

public class CrystalBaseScanner extends BlockScanner
{
    private final PlayerEntity localEntity;
    private final AutoCrystalModule autoCrystal = AutoCrystalModule.INSTANCE;

    public CrystalBaseScanner(int radius, PlayerEntity localEntity)
    {
        super(radius);
        this.localEntity = localEntity;
    }

    @Override
    protected void visit(ClientWorld world, BlockPos pos, BlockState state)
    {
        if (!CrystalUtil.canUseOnBlock(pos))
        {
            return;
        }

        Vec3d explosionCenter = Vec3d.of(pos).add(0.5, 1.0, 0.5);
        double local = ExplosionUtil.damageToEntity(localEntity, explosionCenter);

        for (Entity entity : world.getEntities())
        {
            double blockDist = pos.getSquaredDistance(entity.getPos());
            if (blockDist > 144.0f)
            {
                continue;
            }


        }
    }

}
