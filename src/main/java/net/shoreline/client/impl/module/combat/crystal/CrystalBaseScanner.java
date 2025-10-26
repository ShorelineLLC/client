package net.shoreline.client.impl.module.combat.crystal;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.shoreline.client.impl.block.AsyncBlockState;
import net.shoreline.client.impl.module.combat.AutoCrystalModule;
import net.shoreline.client.impl.module.combat.util.MovementExtrapolation;
import net.shoreline.client.impl.world.EntityState;
import net.shoreline.client.util.entity.PlayerUtil;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class CrystalBaseScanner extends CrystalEntityScanner
{
    private final AutoCrystalModule autoCrystal = AutoCrystalModule.INSTANCE;

    private final List<CrystalData<?>> crystalBases = new CopyOnWriteArrayList<>();

    @Override
    protected void visit(BlockPos pos, AsyncBlockState asyncState)
    {
        Box crystalBB = autoCrystal.getCrystalBox(pos.up());
        boolean blocking = hasEntityBlockingCrystal(crystalBB);
        if (!autoCrystal.canUseOnBlock(this, pos, blocking))
        {
            return;
        }

        float placeRange = autoCrystal.getPlaceRange().getValue();
        double placeDist = getLocalEntity().getEyePos().squaredDistanceTo(pos.toCenterPos());
        if (placeDist > placeRange * placeRange)
        {
            return;
        }

        Vec3d explosionCenter = pos.toBottomCenterPos().add(0.0, 1.0, 0.0);

        Vec3d localPos = getLocalEntity().getPos();
        Box localBox = getLocalEntity().getBoundingBox();
        float local = !PlayerUtil.isInSurvival(MinecraftClient.getInstance().player) ? 0.0f :
                CrystalUtil.getCrystalDamage(this, explosionCenter, localPos, localBox, autoCrystal.getIgnoreTerrain().getValue());

        boolean willKillPlayer = getLocalEntity().getTotalHealth() - local < 0.5f;
        if (local > autoCrystal.getMaxSelfDamage().getValue() || willKillPlayer)
        {
            return;
        }

        for (EntityState entity : getEntities())
        {
            if (!(entity.getEntity() instanceof LivingEntity) || !autoCrystal.canTargetEntity(entity.getEntity()))
            {
                continue;
            }

            int ticks = autoCrystal.getExtrapolateTicks().getValue();
            Vec3d entityPos = ticks <= 0 ? entity.getPos() : MovementExtrapolation.extrapolatePosition(this,
                    entity.getVelocity(),
                    entity.getBoundingBox(),
                    entity.getEntity(),
                    ticks);

            double blockDist = explosionCenter.squaredDistanceTo(entityPos);
            if (blockDist > 144.0f)
            {
                continue;
            }

            float targetRange = autoCrystal.getTargetRange().getValue();
            double dist = getLocalEntity().squaredDistanceTo(entityPos);
            if (dist > targetRange * targetRange)
            {
                continue;
            }

            Box boundingBox = entity.getDimensions().getBoxAt(entityPos);
            float damage = CrystalUtil.getCrystalDamage(this,
                    explosionCenter,
                    entityPos,
                    boundingBox,
                    autoCrystal.getIgnoreTerrain().getValue());

            boolean antiSurround = AntiSurround.checkAntiSurroundQualifiers(this, pos.up());

            crystalBases.add(new CrystalData<>(pos, explosionCenter, entity, damage, local, antiSurround));
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
            Entity entity = entity1.getEntity();
            if (!autoCrystal.canIgnoreEntity(entity, false))
            {
                return true;
            }
        }

        return false;
    }
}
