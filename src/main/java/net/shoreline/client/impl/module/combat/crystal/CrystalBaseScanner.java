package net.shoreline.client.impl.module.combat.crystal;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.shoreline.client.impl.Managers;
import net.shoreline.client.impl.block.AsyncBlockState;
import net.shoreline.client.impl.module.combat.AutoCrystalModule;
import net.shoreline.client.impl.module.combat.util.MovementExtrapolation;
import net.shoreline.client.impl.world.EntityState;
import net.shoreline.client.impl.world.LivingEntityState;
import net.shoreline.client.util.entity.PlayerUtil;
import net.shoreline.client.util.world.RaytraceUtil;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class CrystalBaseScanner extends CrystalEntityScanner
{
    private final List<CrystalData<?>> crystalBases = new CopyOnWriteArrayList<>();

    public CrystalBaseScanner(AutoCrystalModule autoCrystal)
    {
        super(autoCrystal);
    }

    @Override
    protected void visit(BlockPos pos, AsyncBlockState asyncState)
    {
        Box crystalBB = autoCrystal.getCrystalBox(pos.up());
        boolean blocking = hasEntityBlockingCrystal(crystalBB);
        if (!autoCrystal.canUseOnBlock(this, pos, blocking))
        {
            return;
        }

        double placeDist = getLocalEntity().getEyePos().squaredDistanceTo(pos.toCenterPos());
        if (placeDist > MathHelper.square(autoCrystal.getPlaceTrace().getValue())
                && !RaytraceUtil.canSee(new Vec3d(pos.up()), getLocalEntity().getEntity()))
        {
            return;
        }

        if (placeDist > MathHelper.square(autoCrystal.getPlaceRange().getValue()))
        {
            return;
        }

        Vec3d explosionCenter = pos.toBottomCenterPos().add(0.0, 1.0, 0.0);

        Vec3d localPos = getLocalEntity().getPos();
        Box localBox = getLocalEntity().getBoundingBox();
        float local = !PlayerUtil.isInSurvival(MinecraftClient.getInstance().player) ? 0.0f :
                getExplosionDamage(explosionCenter, localPos, localBox, autoCrystal.getIgnoreTerrain().getValue());

        for (LivingEntityState entity : getLivingEntities())
        {
            if (Managers.SOCIAL.isFriend(entity.getName())
                    || entity.getTotalArmor() <= 0 && !autoCrystal.getTargetNakeds().getValue()
                    || !autoCrystal.canTargetEntity(entity.getEntityType()))
            {
                continue;
            }

            int ticks = autoCrystal.getExtrapolateTicks().getValue();
            Vec3d entityPos = ticks <= 0 ? entity.getPos() : MovementExtrapolation.extrapolatePosition(this,
                    box -> getBlockCollisions(entity, box),
                    entity.getVelocity(),
                    entity.getBoundingBox(),
                    ticks);

            double blockDist = explosionCenter.squaredDistanceTo(entityPos);
            if (blockDist > 144.0f)
            {
                continue;
            }

            double dist = getLocalEntity().squaredDistanceTo(entityPos);
            if (dist > MathHelper.square(autoCrystal.getTargetRange().getValue()))
            {
                continue;
            }

            Box boundingBox = entity.getDimensions().getBoxAt(entityPos);
            float damage = getExplosionDamage(explosionCenter,
                    entityPos,
                    boundingBox,
                    autoCrystal.getIgnoreTerrain().getValue());

            crystalBases.add(factory.createData(pos, explosionCenter, entity, damage, local));
        }
    }

    public List<CrystalData<?>> scanCrystalBases()
    {
        crystalBases.clear();
        scanBlocks();
        return crystalBases;
    }

    @Override
    protected int getRadius()
    {
        return (int) Math.ceil(autoCrystal.getTargetRange().getValue());
    }

    private boolean hasEntityBlockingCrystal(Box box)
    {
        for (EntityState entity1 : getOtherEntities(null, box))
        {
            if (!autoCrystal.canIgnoreEntity(entity1.getEntityType(), false))
            {
                return true;
            }
        }

        return false;
    }
}
