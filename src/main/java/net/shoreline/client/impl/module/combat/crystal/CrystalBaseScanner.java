package net.shoreline.client.impl.module.combat.crystal;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.shoreline.client.impl.block.AsyncBlockState;
import net.shoreline.client.impl.module.combat.AutoCrystalModule;
import net.shoreline.client.impl.network.NetworkUtil;
import net.shoreline.client.impl.world.EntityState;
import net.shoreline.client.util.entity.PlayerUtil;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class CrystalBaseScanner extends CrystalEntityScanner
{
    private static final Box FULL_CRYSTAL_BB = new Box(-0.5, 0.0, -0.5, 0.5, 2.0, 0.5);
    private static final Box HALF_CRYSTAL_BB = new Box(-0.5, 0.0, -0.5, 0.5, 1.0, 0.5);

    private final AutoCrystalModule autoCrystal = AutoCrystalModule.INSTANCE;

    private final List<CrystalData<?>> crystalBases = new CopyOnWriteArrayList<>();

    @Override
    protected void visit(BlockPos pos, AsyncBlockState asyncState)
    {
        if (!canUseOnBlock(pos, asyncState.getBlockState()))
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

        float local = !PlayerUtil.isInSurvival(MinecraftClient.getInstance().player) ? 0.0f :
                CrystalUtil.getCrystalDamage(this, explosionCenter, getLocalEntity(), autoCrystal.getIgnoreTerrain().getValue());

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

            double blockDist = explosionCenter.squaredDistanceTo(entity.getPos());
            if (blockDist > 144.0f)
            {
                continue;
            }

            float targetRange = autoCrystal.getTargetRange().getValue();
            double dist = getLocalEntity().squaredDistanceTo(entity.getPos());
            if (dist > targetRange * targetRange)
            {
                continue;
            }

            float damage = CrystalUtil.getCrystalDamage(this, explosionCenter, entity, autoCrystal.getIgnoreTerrain().getValue());

            crystalBases.add(new CrystalData<>(pos, entity, damage, local));
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
        return (int) Math.ceil(autoCrystal.getPlaceRange().getValue());
    }

    private boolean canUseOnBlock(BlockPos pos, BlockState state)
    {
        if (!state.isOf(Blocks.OBSIDIAN) && !state.isOf(Blocks.BEDROCK) && !state.isReplaceable())
        {
            return false;
        }

        if (!autoCrystal.hasSpaceToPlaceCrystal(this, pos))
        {
            return false;
        }

        final Box bb = NetworkUtil.getServerIp().contains("crystalpvp.cc") ? HALF_CRYSTAL_BB : FULL_CRYSTAL_BB;
        return !hasEntityBlockingCrystal(bb.offset(pos.up().toBottomCenterPos()));
    }

    private boolean hasEntityBlockingCrystal(Box box)
    {
        for (EntityState entity1 : getOtherEntities(null, box))
        {
            Entity entity = entity1.getEntity();
            if (entity instanceof ExperienceOrbEntity || autoCrystal.getForcePlace().getValue() && entity instanceof ItemEntity && entity1.getAge() <= 10)
            {
                continue;
            } else if (entity instanceof EndCrystalEntity crystal)
            {
                continue;
            }

            return true;
        }

        return false;
    }
}
